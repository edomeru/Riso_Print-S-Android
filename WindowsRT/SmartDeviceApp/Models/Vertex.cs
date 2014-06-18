using SharpDX;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Models
{
    public class Vertex
    {
        

        public Vertex()
        {

        }

        public Vector3 Position
        {
            get;
            set;
        }


        public SharpDX.Color Color
        {
            get;
            set;
        }

    }
}
