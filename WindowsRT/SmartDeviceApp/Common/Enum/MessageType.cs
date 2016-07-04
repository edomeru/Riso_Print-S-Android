using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Common.Enum
{
    /// <summary>
    /// Enumeration of type of message used when sending strings thru Messenger.
    /// </summary>
    public enum MessageType
    {
        AddPrinter,
        RightPageImageUpdated,
        SnmpCommunityNamePasteInvalid
    }
}