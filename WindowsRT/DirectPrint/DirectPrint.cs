using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Runtime.InteropServices;
using DirectPrint;

namespace DirectPrint
{
    public class DirectPrint{

        DirectPrintSettingsWrapper d;

        //constructor
        public DirectPrint(){

            d = new DirectPrintSettingsWrapper();

        }

        public void CreatePJL(String pjl, String settings){

            byte[] p = new byte[] { 0, 0, 0 };
            byte[] s = new byte[] { 0, 0, 0 };
            DirectPrintSettingsWrapper.create_pjl_wrapper("test", "test");

        }
    }
}
