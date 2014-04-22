using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Networking;
using Windows.Storage;
using Windows.Storage.FileProperties;
using Windows.Storage.Pickers;
using Windows.Storage.Streams;

namespace DirectPrint
{
    public class directprint_job
    {
        public string job_name;
        public string filename;
        public string print_settings;
        public string ip_address;
        //directprint_callback callback;

        //pthread_t main_thread;
        //pthread_mutex_t mutex;
        public float progress;
        public int cancel_print;

        //void* caller_data;
    };

    public class DirectPrint
    {
        string PORT_LPR = "515";
        string PORT_RAW = "9100";

        int TIMEOUT_CONNECT = 10;
        int TIMEOUT_RECEIVE = 30;

        int BUFFER_SIZE = 4096;

        string QUEUE_NAME = "normal";
        string HOST_NAME = "SmartDeviceApp";

        string PJL_ESCAPE = "\x1B-12345X";
        string PJL_LANGUAGE = "@PJL ENTER LANGUAGE = PDF\x0d\x0a";
        string PJL_EOJ = "@PJL EOJ\x0d\x0a";

        float LPR_PREP_PROGRESS_STEP = 6.0f;
        float LPR_PREP_END_PROGRESS = 20.0f;
        float PJL_HEADER_PROGRESS_STEP = 5.0f;
        float PJL_FOOTER_PROGRESS_STEP = 4.0f;
        float END_PROGRESS = 100.0f;

        TCPSocket socket;

        public DirectPrint()
        {
            directprint_job job = new directprint_job();
            job.job_name = "jobname";
            job.filename = "test.pdf";
            job.print_settings = "";
            job.ip_address = "192.168.1.21";//21
            job.progress = 0;
            job.cancel_print = 0;

            socket = new TCPSocket();
            socket.connect(job.ip_address, PORT_LPR);
            socket.assignDelegate(receiveData);

            IAsyncAction asyncAction = Windows.System.Threading.ThreadPool.RunAsync(
            (workItem) =>
            {
                startLPRPrint(job);
            });
        }


        public async void startLPRPrint(directprint_job parameter)
        {
            //FileOpenPicker openPicker = new FileOpenPicker();
            //openPicker.FileTypeFilter.Add(".pdf");
            //StorageFile filePickerFile = await openPicker.PickSingleFileAsync();

            directprint_job print_job = parameter;
    
            // Prepare PJL header
            string pjl_header = "";
            pjl_header += PJL_ESCAPE;
            //***create_pjl(pjl_header, print_job->print_settings);
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
            const int BUFSIZE = 1024;				// 1KB buffer 
            byte[] buffer = new byte[BUFSIZE];

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
            socket.write(buffer, 0, pos);
            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
                return;
            }

            // CONTROL FILE : Prepare
            string dname = String.Format("dfA{0}{1}", 1, HOST_NAME);
            string cname = String.Format("cfA{0}{1}", 1, HOST_NAME);
            string controlfile = String.Format("H{0}\nP{1}\nJ{2}\nf{3}\nU{4}\nN{5}\n",
                                        HOST_NAME, "User", print_job.job_name, dname, dname, print_job.job_name);
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
            string controlfile_length_str = String.Format("{0}", controlfile.Length);
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
            socket.write(buffer, 0, pos);
            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
                string test = System.Text.Encoding.UTF8.GetString(buffer,0,pos);
                return;
            }

            /////////////////////////////////////////////////////////
            /// ADD CONTENT OF CONTROLFILE
            pos = 0;
            for (i = 0; i < controlfile.Length; i++)
            {
                buffer[pos++] = (byte)controlfile[i];
            }
            buffer[pos++] = 0;

            //***write buffer to socket
            socket.write(buffer, 0, pos);
            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
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
            buffer[pos++] = 0x3;


            // Get file size
            var uri = new System.Uri("ms-appx:///Resources/Dummy/test.pdf");//UriSource = new Uri("ms-appx:///Resources/Dummy/" + filename);
            StorageFile sampleFile = await Windows.Storage.StorageFile.GetFileFromApplicationUriAsync(uri);
            //Windows.Storage.StorageFolder localFolder = Windows.Storage.ApplicationData.Current.;
            //Windows.Storage.StorageFolder localFolder = Windows.ApplicationModel.Package.Current.;
            //StorageFile sampleFile = await localFolder.GetFileAsync("test.pdf");
            //StorageFile sampleFile = filePickerFile;

            var fbuffer = await Windows.Storage.FileIO.ReadBufferAsync(sampleFile);
            DataReader fileDataReader = Windows.Storage.Streams.DataReader.FromBuffer(fbuffer);
            BasicProperties pro = await sampleFile.GetBasicPropertiesAsync();
            ulong file_size = pro.Size;

            byte[] filebuffer = new byte[file_size];
            fileDataReader.ReadBytes(filebuffer);


            ulong total_data_size = (ulong)file_size;// +(ulong)pjl_header_size + (ulong)pjl_footer_size;
            String total_data_size_str = String.Format("{0}", (long)total_data_size);

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
            socket.write(buffer, 0, pos);
            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
                return;
            }

            //send file data
            

            //***write buffer to socket
            socket.write(filebuffer, 0, pos);
            /////////////////////////////////////////////////////////
            /// READ ACK
            if (waitForAck() != 0)
            {
                return;
            }

            return;
            //end!
        }

        private int waitForAck()
        {
            while (!datareceived)
            {
                // wait for data
            }
            datareceived = false;
            
            return ack;
        }


        private bool datareceived = false;
        private int ack = 0;
        public void receiveData(HostName hostname, byte[] data)
        {
            if (data != null)
            {
                datareceived = true;
                ack = data[0];
            }
        }
    }
}
