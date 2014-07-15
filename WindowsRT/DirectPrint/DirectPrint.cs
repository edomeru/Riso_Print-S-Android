using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Networking;
using Windows.Storage;
using Windows.Storage.FileProperties;
using Windows.Storage.Pickers;
using Windows.Storage.Streams;

namespace DirectPrint
{
    public delegate void directprint_callback(int directprint_result);
    public delegate void progress_callback(float progress);

    public class directprint_job
    {
        public string job_name;
        //public string filename; // TODO: to be deleted. replaced by file
        public StorageFile file;
        public string username;
        public string print_settings;
        public string ip_address;
        public directprint_callback callback;
        public progress_callback progress_callback;


        //pthread_t main_thread;
        //pthread_mutex_t mutex;
        public float progress;
        public int cancel_print;

        //void* caller_data;
    }

    public class DirectPrint
    {
        public const int PRINT_STATUS_OK = 0;
        public const int PRINT_STATUS_ERROR = 1;
        public const int PRINT_STATUS_CANCELLED = 2;
        public const int PRINT_STATUS_NO_NETWORK = 3;

        private const string PORT_LPR = "515";
        private const string PORT_RAW = "9100";

        private const int TIMEOUT_CONNECT = 10000;
        private const int TIMEOUT_RECEIVE = 10000;

        private const int BUFFER_SIZE = 4096;

        private const string QUEUE_NAME = "normal";
        private const string HOST_NAME = "SmartDeviceApp";

        private const string PJL_ESCAPE = "\x1B%-12345X";
        private const string PJL_LANGUAGE = "@PJL ENTER LANGUAGE = PDF\x0d\x0a";
        private const string PJL_EOJ = "@PJL EOJ\x0d\x0a";

        private const float LPR_PREP_PROGRESS_STEP = 6.0f;
        private const float LPR_PREP_END_PROGRESS = 20.0f;
        private const float PJL_HEADER_PROGRESS_STEP = 5.0f;
        private const float PJL_FOOTER_PROGRESS_STEP = 4.0f;
        private const float END_PROGRESS = 100.0f;

        private bool datareceived = false;
        private int ack = 0;

        TCPSocket socket;

#if DEBUG
        private Stream printJobStream;
        private const string PDF_DATA = "**** PDF DATA ****\x0d\x0a";
#endif

        private void nullCallBack(int val)
        {

        }

        private directprint_job print_job = null;

        /// <summary>
        /// Constructor for DirectPrint.
        /// </summary>
        public DirectPrint()
        {

        }

        /// <summary>
        /// Cancels the printing.
        /// </summary>
        public void cancelPrint()
        {
            if (print_job != null)
            {
                print_job.cancel_print = 1;
            }
            /*
            if (t != null)
            {
                tokenSource.Cancel();
            }
            */

        }

        //CancellationTokenSource tokenSource = new CancellationTokenSource();
        //Task t = null;

        /// <summary>
        /// Starts LPR printing.
        /// </summary>
        /// <param name="parameter">direct print job parameters</param>
        public async void startLPRPrint(directprint_job parameter)
        {
            /*
            IAsyncAction asyncAction = Windows.System.Threading.ThreadPool.RunAsync(
            (workItem) =>
            {
                _startLPRPrint(parameter);
            });
            */

#if DEBUG
            // Dump print job to temp file
            using (printJobStream = await ApplicationData.Current.TemporaryFolder.OpenStreamForWriteAsync(
                String.Format("{0}.txt", DateTime.Now.ToString("yyyyMMddHHmmss")),
                CreationCollisionOption.ReplaceExisting))
            {
                printJobStream.Seek(0, SeekOrigin.End);
#endif
                /*
                //TODO: partial implementation of cancellation token method.
                {

                    t = Task.Factory.StartNew(async () =>
                    {
                        await _startLPRPrint(parameter);
                    },tokenSource);
                }
                */
                await Task.Run(() => _startLPRPrint(parameter));                        
#if DEBUG
            }
#endif
        }

        /// <summary>
        /// Starts LPR printing.
        /// </summary>
        /// <param name="parameter">direct print job parameter</param>
        /// <returns></returns>
        public async Task _startLPRPrint(directprint_job parameter)
        {
            callbackTriggered = false;

            if (parameter == null)
            {
                triggerCallback(PRINT_STATUS_ERROR);
                if (socket != null) socket.disconnect();
                return;
            }

            print_job = parameter;

            //start socket
            socket = new TCPSocket(print_job.ip_address, PORT_LPR, receiveData, timeout);
            int connectretries = 0;
            int maxretries = 0;
            //while (connectretries <= maxretries)
            {
                try
                {
                    await socket.connect();
                    connectretries = maxretries;
                }
                catch (Exception)
                {
                    //if (connectretries++ >= maxretries)
                    {
                        triggerCallback(PRINT_STATUS_ERROR);
                        if (socket != null) socket.disconnect();
                        return;
                    }
                }
            }

            // Prepare PJL header
            string pjl_header = "";
            pjl_header += PJL_ESCAPE;
            pjl_header += DirectPrintSettingsWrapper.create_pjl_wrapper(print_job.print_settings);
            pjl_header += PJL_LANGUAGE;
            int pjl_header_size = pjl_header.Length;

            // Prepare PJL footer
            string pjl_footer = "";
            pjl_footer += PJL_ESCAPE;
            pjl_footer += PJL_EOJ;
            pjl_footer += PJL_ESCAPE;
            int pjl_footer_size = pjl_footer.Length;

            ////

            // Prepare flags
            int pos;
            int i;

            ////////////////////////////////////////////////////////
            /// LOCAL VARIABLES
            ///
            byte[] buffer = new byte[BUFFER_SIZE];

            ////////////////////////////////////////////////////////
            /// COMMAND: Receive a printer job
            ///      +----+-------+----+
            ///      | 02 | Queue | LF |
            ///      +----+-------+----+
            ///      Command code - 2
            ///      Operand - Printer queue name
            /// 

            pos = 0;
            buffer[pos++] = 0x2;
            for (i = 0; i < QUEUE_NAME.Length; i++)
            {
                buffer[pos++] = (byte)(QUEUE_NAME.ToCharArray()[i]);
            }
            buffer[pos++] = (byte)'\n';

            //***write buffer to socket
            try
            {
                await socket.write(buffer, 0, pos);
            }
            catch (Exception e)
            {
                triggerCallback(PRINT_STATUS_ERROR);
                if (socket != null) socket.disconnect();
                return;
            }
            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
                triggerCallback(PRINT_STATUS_ERROR);
                if (socket != null) socket.disconnect();
                return;
            }
            print_job.progress += LPR_PREP_PROGRESS_STEP;
            if (print_job.progress_callback != null) print_job.progress_callback(print_job.progress);
            if (print_job.cancel_print == 1)
            {
                triggerCallback(PRINT_STATUS_CANCELLED);
                if (socket != null) socket.disconnect();
                return;
            }


            // CONTROL FILE : Prepare
            string username = print_job.username;
            string dname = String.Format("dfA{0}{1}", 1, HOST_NAME);
            string cname = String.Format("cfA{0}{1}", 1, HOST_NAME);
            string controlfile = String.Format("H{0}\nP{1}\nJ{2}\nf{3}\nU{4}\nN{5}\n",
                                        HOST_NAME, username, print_job.job_name, dname, dname, print_job.job_name);
            byte[] controlfilebytes = System.Text.Encoding.UTF8.GetBytes(controlfile);
            //string controlfile = String.Format("H{0}\nP{1}\nf{2}\nU{3}\nN{4}\n",
            //                HOST_NAME, "User", dname, dname, print_job.job_name);
            /////////////////////////////////////////////////////////
            /// SUBCMD: RECEIVE CONTROL FILE
            ///
            ///      +----+-------+----+------+----+
            ///      | 02 | Count | SP | Name | LF |
            ///      +----+-------+----+------+----+
            ///      Command code - 2
            ///      Operand 1 - Number of bytes in control file
            ///      Operand 2 - Name of control file
            ///
            pos = 0;
            buffer[pos++] = 0x2;
            string controlfile_length_str = controlfilebytes.Length.ToString();//controlfile.Length.ToString();
            for (i = 0; i < controlfile_length_str.Length; i++)
            {
                buffer[pos++] = (byte)controlfile_length_str[i];
            }
            buffer[pos++] = (byte)' ';
            for (i = 0; i < cname.Length; i++)
            {
                buffer[pos++] = (byte)cname[i];
            }
            buffer[pos++] = (byte)'\n';

            //***write buffer to socket
            await socket.write(buffer, 0, pos);

            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
                triggerCallback(PRINT_STATUS_ERROR);
                if (socket != null) socket.disconnect();
                return;
            }
            print_job.progress += LPR_PREP_PROGRESS_STEP;
            if (print_job.progress_callback != null) print_job.progress_callback(print_job.progress);

            /////////////////////////////////////////////////////////
            /// ADD CONTENT OF CONTROLFILE
            pos = 0;
            for (i = 0; i < controlfilebytes.Length; i++)
            {
                buffer[pos++] = controlfilebytes[i];
            }
            buffer[pos++] = 0;

            //***write buffer to socket
            await socket.write(buffer, 0, pos);
            string test001 = System.Text.Encoding.UTF8.GetString(buffer, 0, pos);
            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
                triggerCallback(PRINT_STATUS_ERROR);
                if (socket != null) socket.disconnect();
                return;
            }

            if (print_job.cancel_print == 1)
            {
                triggerCallback(PRINT_STATUS_CANCELLED);
                if (socket != null) socket.disconnect();
                return;
            }



            /////////////////////////////////////////////////////////
            /// SUBCMD: RECEIVE DATA FILE
            ///
            ///      +----+-------+----+------+----+
            ///      | 03 | Count | SP | Name | LF |
            ///      +----+-------+----+------+----+
            ///      Command code - 3
            ///      Operand 1 - Number of bytes in data file
            ///      Operand 2 - Name of data file
            ///
            pos = 0;
            buffer[pos++] = 3;
            print_job.progress = LPR_PREP_END_PROGRESS;
            if (print_job.progress_callback != null) print_job.progress_callback(print_job.progress);

            // Get file size
            //var uri = new System.Uri("ms-appx:///Resources/Dummy/" + print_job.filename);//RZ1070.pdf//UriSource = new Uri("ms-appx:///Resources/Dummy/" + filename);
            //StorageFile sampleFile = await Windows.Storage.StorageFile.GetFileFromApplicationUriAsync(uri);
            //Windows.Storage.StorageFolder localFolder = Windows.Storage.ApplicationData.Current.;
            //Windows.Storage.StorageFolder localFolder = Windows.ApplicationModel.Package.Current.;
            //StorageFile sampleFile = await localFolder.GetFileAsync("test.pdf");
            //StorageFile sampleFile = filePickerFile;

            var fbuffer = await Windows.Storage.FileIO.ReadBufferAsync(print_job.file);
            DataReader fileDataReader = Windows.Storage.Streams.DataReader.FromBuffer(fbuffer);
            BasicProperties pro = await print_job.file.GetBasicPropertiesAsync();
            ulong file_size = pro.Size;

            byte[] filebuffer = new byte[file_size];
            fileDataReader.ReadBytes(filebuffer);


            ulong total_data_size = (ulong)file_size +(ulong)pjl_header_size + (ulong)pjl_footer_size;
            String total_data_size_str = String.Format("{0}", (ulong)total_data_size);

            float data_step = (70.0f / ((float)file_size / BUFFER_SIZE));

            for (i = 0; i < total_data_size_str.Length; i++)
            {
                buffer[pos++] = (byte)total_data_size_str[i];
            }
            buffer[pos++] = (byte)' ';
            for (i = 0; i < dname.Length; i++)
            {
                buffer[pos++] = (byte)dname[i];
            }
            buffer[pos++] = (byte)'\n';

            //***write buffer to socket
            await socket.write(buffer, 0, pos);
            string test002 = System.Text.Encoding.UTF8.GetString(buffer, 0, pos);

            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
                triggerCallback(PRINT_STATUS_ERROR);
                if (socket != null) socket.disconnect();
                return;
            }
            if (print_job.cancel_print == 1)
            {
                triggerCallback(PRINT_STATUS_CANCELLED);
                if (socket != null) socket.disconnect();
                return;
            }

            //send file data in chunks            
            ulong totalbytes = 0;
            int bytesRead = 0;

            await socket.write(System.Text.Encoding.UTF8.GetBytes(pjl_header), 0, pjl_header.Length);
#if DEBUG
            await printJobStream.WriteAsync(System.Text.Encoding.UTF8.GetBytes(pjl_header), 0, pjl_header.Length);
            await printJobStream.WriteAsync(System.Text.Encoding.UTF8.GetBytes(PDF_DATA), 0, PDF_DATA.Length);
#endif
            totalbytes += (ulong)pjl_header.Length;

            MemoryStream fstream = new MemoryStream(filebuffer);
            while ((bytesRead = fstream.Read(buffer, 0, BUFFER_SIZE)) > 0)
            {
                totalbytes += (ulong)bytesRead;
                await socket.write(buffer, 0, bytesRead);
                print_job.progress += data_step;
                if (print_job.progress_callback != null) print_job.progress_callback(print_job.progress);

                if (print_job.cancel_print == 1)
                {
                    triggerCallback(PRINT_STATUS_ERROR);
                    if (socket != null) socket.disconnect();
                    return;
                }
            }
            await socket.write(System.Text.Encoding.UTF8.GetBytes(pjl_footer), 0, pjl_footer.Length);
#if DEBUG
            await printJobStream.WriteAsync(System.Text.Encoding.UTF8.GetBytes(pjl_footer), 0, pjl_footer.Length);
#endif
            totalbytes += (ulong)pjl_footer.Length;

            if (total_data_size != totalbytes)
            {
                triggerCallback(PRINT_STATUS_ERROR);
                if (socket != null) socket.disconnect();
                return;
            }

            // close data file with a 0
            pos = 0;
            buffer[pos++] = 0;
            await socket.write(buffer, 0, pos);
            print_job.progress = 99.0f;
            if (print_job.progress_callback != null) print_job.progress_callback(print_job.progress);

            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
                triggerCallback(PRINT_STATUS_ERROR);
                if (socket != null) socket.disconnect();
                return;
            }
            if (print_job.cancel_print == 1)
            {
                triggerCallback(PRINT_STATUS_CANCELLED);
                if (socket != null) socket.disconnect();
                return;
            }
            

            print_job.progress = END_PROGRESS;
            if (print_job.progress_callback != null) print_job.progress_callback(print_job.progress);
            triggerCallback(PRINT_STATUS_OK);
            if (socket != null) socket.disconnect();

            return;
            //end!
        }

        private void triggerCallback(int status)
        {
            if (callbackTriggered) return;
            callbackTriggered = true;

            if (print_job.callback != null)
            {
                print_job.callback(status);
            }
        }

        private int waitForAck()
        {            
            long start = Environment.TickCount;
            datareceived = false;
            socket.read();
            while (!datareceived)
            {
                if (callbackTriggered) return -1;

                if (Environment.TickCount - start > TIMEOUT_RECEIVE)
                {
                    triggerCallback(PRINT_STATUS_ERROR);
                    return -1;
                }
            }
            datareceived = false;

            return ack;
        }

        /// <summary>
        /// Receives data from host.
        /// </summary>
        /// <param name="hostname">Hostname where data came from.</param>
        /// <param name="data">data</param>
        public void receiveData(HostName hostname, byte data)
        {
            datareceived = true;
            ack = data;
        }

        private bool callbackTriggered = false;
        /// <summary>
        /// Handles time out.
        /// </summary>
        /// <param name="hostname">Hostname where time out occurred.</param>
        /// <param name="data">data</param>
        public void timeout(HostName hostname, byte data)
        {
            if (print_job != null) print_job.cancel_print = 1;
            triggerCallback(PRINT_STATUS_NO_NETWORK);
        }
    }
}
