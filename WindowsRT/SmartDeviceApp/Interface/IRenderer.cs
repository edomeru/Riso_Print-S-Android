using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using CommonDX;

namespace SmartDeviceApp.Interface
{
        public interface IRenderer
        {
            void Initialize(DeviceManager deviceManager);
            void InitializeUI(Windows.UI.Xaml.UIElement rootForPointerEvents, Windows.UI.Xaml.UIElement rootOfLayout);
            void Render(TargetBase target);

            void LoadLocalAsset(string assetUri);

        }
}
