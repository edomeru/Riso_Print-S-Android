using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Models
{
    public class CurlParameters
    {
        float _theta;  // Angle of right-cone
        float _ay;     // Location on y axis of cone apex
        float _alpha;  // Rotation about y axis
        float _conicContribution;  // South tip cone == -1, cylinder == 0,north tip cone == 1

        public CurlParameters()
        {
            this.Theta = 0;
            this.Ay = 0;
            this.Alpha = 0;
            this.ConicContribution = 0;
        }

        public CurlParameters(float t, float a, float ang, float c)
        {
            this.Theta = t;
            this.Ay = a;
            this.Alpha = ang;
            this.ConicContribution = c;
        }

        public float Theta
        {
            get { return _theta; }
            set { _theta = value; }
        }

        public float Ay
        {
            get { return _ay; }
            set { _ay = value; }
        }

        public float Alpha
        {
            get { return _alpha; }
            set { _alpha = value; }
        }

        public float ConicContribution
        {
            get { return _conicContribution; }
            set { _conicContribution = value; }
        }
    }
}
