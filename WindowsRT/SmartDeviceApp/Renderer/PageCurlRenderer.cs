using CommonDX;
using GalaSoft.MvvmLight.Messaging;
using SharpDX;
using SharpDX.Direct2D1;
using SharpDX.Direct3D;
using SharpDX.Direct3D11;
using SharpDX.DXGI;
using SharpDX.IO;
using SharpDX.WIC;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Interface;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;

namespace SmartDeviceApp.Renderer
{
    public class PageCurlRenderer: Component , IRenderer
    {
        private DeviceManager _deviceManager;


        public bool EnableClear { get; set; }

        public bool Show { get; set; }


        private UIElement _root;
        private DependencyObject _rootParent;
        private Stopwatch clock;

        private float _appWidth;
        private float _appHeight;

        public PageCurlControl DrawingSurface;


        private static double _diameter = 0.1f;
        private static int _numberOfSprites = 140;
        private Vector2 _spriteSize = new Vector2((float)_diameter, (float)_diameter);
        private List<Particle2> _particles = new List<Particle2>();
        private Random _rand = new Random();
        private Vector2 _spriteStartPosition = new Vector2(0, 0);
        //private Windows.UI.Color _spriteColor;

        private SharpDX.Direct3D11.Buffer constantBufferVS;
        private SharpDX.Direct3D11.Buffer constantBufferPS;
        private InputLayout layout;
        private VertexBufferBinding vertexBufferBinding;
        private VertexShader vertexShader;
        private PixelShader pixelShader;
        private ShaderResourceView textureView;
        private SamplerState sampler;
        private BlendState1 m_blendStateAlpha; //cruicial to ensure the sprites are blended nicely over each other
        
        private Matrix _view; // The view or camera transform
        private Matrix _projection; // The projection transform to convert 3D space to 2D screen space
        private Matrix _viewProj;


        private bool _moveBurst = false;
        private bool _showBurst = false;

        private SharpDX.Direct3D11.Buffer vertexBuffer;

        /// <summary>
        /// Initializes a new instance of <see cref="Rectangle"/>
        /// </summary>
        public PageCurlRenderer()
        {

        }

    
        public float Scale { get; set; }

        private SharpDX.Direct3D11.Buffer constantBuffer;

        int _initOn2 = 0;
        public virtual void Initialize(DeviceManager devices)
        {

            _deviceManager = devices;

            var size = _deviceManager.ContextDirect2D.Size;
            int pixelWidth = (int)(size.Width * Windows.Graphics.Display.DisplayProperties.LogicalDpi / 96.0);
            int pixelHeight = (int)(size.Height * Windows.Graphics.Display.DisplayProperties.LogicalDpi / 96.0);

            //setup vertices

            var path = Windows.ApplicationModel.Package.Current.InstalledLocation.Path;

            // Loads vertex shader bytecode
            var vertexShaderByteCode = NativeFile.ReadAllBytes(path + "\\Assets\\MiniCube_VS.fxo");
            vertexShader = new VertexShader(devices.DeviceDirect3D, vertexShaderByteCode);

            layout = new InputLayout(devices.DeviceDirect3D, vertexShaderByteCode, new[]
                    {
                        new SharpDX.Direct3D11.InputElement("POSITION", 0, Format.R32G32B32A32_Float, 0, 0),
                        new SharpDX.Direct3D11.InputElement("COLOR", 0, Format.R32G32B32A32_Float, 16, 0)
                    });

            // Loads pixel shader bytecode
            pixelShader = new PixelShader(devices.DeviceDirect3D, NativeFile.ReadAllBytes(path + "\\Assets\\MiniCube_PS.fxo"));
            constantBuffer = ToDispose(new SharpDX.Direct3D11.Buffer(devices.DeviceDirect3D, Utilities.SizeOf<Matrix>(), ResourceUsage.Default, BindFlags.ConstantBuffer, CpuAccessFlags.None, ResourceOptionFlags.None, 0));

            Scale = 1.0f;
            

        }

        private SharpDX.Point _startPoint;
        private SharpDX.Point _endPoint;


        SharpDX.Direct2D1.Bitmap direct2DBitmap;
        public virtual void Render(TargetBase target)
        {
            var context2D = target.DeviceManager.ContextDirect2D;

            context2D.BeginDraw();

            context2D.Clear(SharpDX.Color.Transparent);

            context2D.DrawRectangle(new RectangleF(0, 0, _appWidth - (_appWidth - _startPoint.X), _appHeight), new SolidColorBrush(context2D, Color.Beige));


            
            //direct2DBitmap = SharpDX.Direct2D1.Bitmap.FromWicBitmap(context2D, Getimage(target.DeviceManager));
            
            //RectangleF rect = new RectangleF(0, 0, _appWidth, _appHeight);
            //context2D.DrawBitmap(direct2DBitmap, rect, 1.0f, SharpDX.Direct2D1.BitmapInterpolationMode.Linear);

            var brush = new SolidColorBrush(_deviceManager.ContextDirect2D, SharpDX.Color.Black);

            context2D.DrawLine(_startPoint, _endPoint, brush, 4);



            
            System.Diagnostics.Debug.WriteLine("Height: ");
            System.Diagnostics.Debug.WriteLine(_appHeight);



            context2D.EndDraw();

            //TODO: insert curling here 
            RenderCurl();

            var context3D = target.DeviceManager.ContextDirect3D;
            //vertexBuffer = SharpDX.Direct3D11.Buffer.Create(target.DeviceManager.DeviceDirect3D, BindFlags.VertexBuffer, _vertexBuffer);

            //float width = (float)target.RenderTargetSize.Width;
            //float height = (float)target.RenderTargetSize.Height;
            //// Prepare matrices


            //context3D.OutputMerger.SetTargets(target.DepthStencilView, target.RenderTargetView);


            //var vertexBufferBinding = new VertexBufferBinding(vertexBuffer, Utilities.SizeOf<Vector3>(), 0);

            //// Calculate WorldViewProj
            //var worldViewProj = Matrix.Scaling(Scale) * viewProj;
            //worldViewProj.Transpose();

            //////w
            //context3D.InputAssembler.SetVertexBuffers(0, vertexBufferBinding);
            //context3D.InputAssembler.InputLayout = layout;
            //context3D.InputAssembler.PrimitiveTopology = PrimitiveTopology.TriangleStrip; //triangle strip used in austin
            //context3D.VertexShader.SetConstantBuffer(0, constantBuffer);
            //context3D.VertexShader.Set(vertexShader);
            //context3D.PixelShader.Set(pixelShader);

            //context3D.UpdateSubresource(ref worldViewProj, constantBuffer, 0);

            //context3D.Draw(_vertexBuffer.Count(), 0);


            //==========================
            int width = 100;
            int height = 100;
            int index = 0;

            var _vertexCount = (width - 1) * (height - 1) * 8;

            Vector4[] vertices = new Vector4[_vertexCount * 2];
            int[] indices = new int[vertices.Length];
            float positionW = 1f;



            for (int j = 0; j < height - 1; j++)
            {
                for (int i = 0; i < width - 1; i++)
                {

                    // LINE 1
                    // Upper left.
                    float positionX = (float)i;
                    float positionZ = (float)(j + 1);
                    vertices[index] = new Vector4(positionX, 0.0f, positionZ, positionW);
                    index++;
                    vertices[index] = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
                    index++;


                    // Upper right.
                    positionX = (float)(i + 1);
                    positionZ = (float)(j + 1);
                    vertices[index] = new Vector4(positionX, 0.0f, positionZ, positionW);
                    index++;
                    vertices[index] = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
                    index++;

                    // LINE 2
                    // Upper right.
                    positionX = (float)(i + 1);
                    positionZ = (float)(j + 1);
                    vertices[index] = new Vector4(positionX, 0.0f, positionZ, positionW);
                    index++;
                    vertices[index] = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
                    index++;

                    // Bottom right.
                    positionX = (float)(i + 1);
                    positionZ = (float)j;
                    vertices[index] = new Vector4(positionX, 0.0f, positionZ, positionW);
                    index++;
                    vertices[index] = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
                    index++;

                    // LINE 3
                    // Bottom right.
                    positionX = (float)(i + 1);
                    positionZ = (float)j;
                    vertices[index] = new Vector4(positionX, 0.0f, positionZ, positionW);
                    index++;
                    vertices[index] = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
                    index++;

                    // Bottom left.
                    positionX = (float)i;
                    positionZ = (float)j;
                    vertices[index] = new Vector4(positionX, 0.0f, positionZ, positionW);
                    index++;
                    vertices[index] = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
                    index++;

                    // LINE 4
                    // Bottom left.
                    positionX = (float)i;
                    positionZ = (float)j;
                    vertices[index] = new Vector4(positionX, 0.0f, positionZ, positionW);
                    index++;
                    vertices[index] = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
                    index++;

                    // Upper left.
                    positionX = (float)i;
                    positionZ = (float)(j + 1);
                    vertices[index] = new Vector4(positionX, 0.0f, positionZ, positionW);
                    index++;
                    vertices[index] = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
                    index++;
                }
            }

            var _vertices = SharpDX.Direct3D11.Buffer.Create(target.DeviceManager.DeviceDirect3D, BindFlags.VertexBuffer, vertices);
            vertices = null;

            // Create Contant Buffer
            var _contantBuffer = new SharpDX.Direct3D11.Buffer(target.DeviceManager.DeviceDirect3D, Utilities.SizeOf<Matrix>(), ResourceUsage.Default, BindFlags.ConstantBuffer, CpuAccessFlags.None, ResourceOptionFlags.None, 0);

            context3D.InputAssembler.InputLayout = layout;
            context3D.InputAssembler.PrimitiveTopology = PrimitiveTopology.LineList;
            context3D.InputAssembler.SetVertexBuffers(0, new VertexBufferBinding(_vertices, Utilities.SizeOf<Vector4>() + Utilities.SizeOf<Vector4>(), 0));
            context3D.VertexShader.SetConstantBuffer(0, _contantBuffer);
            context3D.VertexShader.Set(vertexShader);
            context3D.PixelShader.Set(pixelShader);

            var view = Matrix.LookAtLH(new Vector3(0, 0, -5), new Vector3(0, 0, 0), Vector3.UnitY);
            var proj = Matrix.PerspectiveFovLH((float)Math.PI / 4.0f, width / (float)height, 0.1f, 100.0f);
            var viewProj = Matrix.Multiply(view, proj);
            //// Calculate WorldViewProj
            var worldViewProj = Matrix.Scaling(Scale) * viewProj;
            worldViewProj.Transpose();
            // Update WorldViewProj Matrix

            context3D.UpdateSubresource(ref worldViewProj, _contantBuffer);

            // Draw the _cube
            context3D.Draw(_vertexCount, 0);
           

        }

        // getting image
        private FormatConverter Getimage(DeviceManager deviceManager)
        {
            var bitMap = new BitmapDecoder(deviceManager.WICFactory, @"Assets\San_Francisco_City.jpg", SharpDX.IO.NativeFileAccess.Read, DecodeOptions.CacheOnDemand);
            BitmapFrameDecode bitmapFrame = bitMap.GetFrame(0);
            BitmapSource bitmapSource = new BitmapSource(bitmapFrame.NativePointer);
            FormatConverter format = new FormatConverter(deviceManager.WICFactory);
            format.Initialize(bitmapSource, SharpDX.WIC.PixelFormat.Format32bppPBGRA);
            return format;
        }

        public void InitializeUI(Windows.UI.Xaml.UIElement rootForPointerEvents, Windows.UI.Xaml.UIElement rootOfLayout)
        {
            _root = rootForPointerEvents;
            _rootParent = rootOfLayout;

            _appWidth = (float)((FrameworkElement)_root).ActualWidth;
            _appHeight = (float)((FrameworkElement)_root).ActualHeight;

            _vertexCountX = 31;
            _vertexCountY = 41;

            var vertexBufferCount = _vertexCountX * _vertexCountY;

            _vertexBuffer = new Vector4[vertexBufferCount];


            //WIRE UP : POINTER EVENTS
            _root.PointerMoved += _root_PointerMoved;
            _root.PointerPressed += _root_PointerPressed;
            _root.PointerReleased += _root_PointerReleased;



            _view = Matrix.LookAtLH(new Vector3(0, 0, -10), new Vector3(0, 0, 0), Vector3.UnitY);
            _projection = Matrix.PerspectiveFovLH((float)Math.PI / 4.0f, _appWidth / (float)_appHeight, 0.1f, 100.0f);
            _viewProj = Matrix.Multiply(_view, _projection);
            RenderCurl();

        }
        private bool _willDraw;
        public void _root_PointerReleased(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            _willDraw = false;
        }

        public void _root_PointerPressed(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            _willDraw = true;
            startUserCurl((float)e.GetCurrentPoint((UIElement)sender).Position.X, (float)e.GetCurrentPoint((UIElement)sender).Position.Y);
        }

        public void _root_PointerMoved(object sender, Windows.UI.Xaml.Input.PointerRoutedEventArgs e)
        {
            if (_willDraw)
            {
                //TODO: adjust the points for drawing the curl

                //test drawing line
                var size = _deviceManager.ContextDirect2D.Size;
                _startPoint.Y = 0;
                _startPoint.X = (int)e.GetCurrentPoint((UIElement)sender).Position.X;
                _startPoint.Y = (int)(size.Height * Windows.Graphics.Display.DisplayProperties.LogicalDpi / 96.0); ;
                _endPoint.X = (int)e.GetCurrentPoint((UIElement)sender).Position.X;
            }
            
        }


        private void moveDot(Windows.UI.Input.PointerPoint newPosition)
        {
            if (!_moveBurst) return;
            
            _spriteStartPosition.X = (float)((newPosition.Position.X - (_appWidth / 2)) * _diameter);
            _spriteStartPosition.Y = (float)((newPosition.Position.Y - (_appHeight / 2)) * _diameter);
        }

        public void LoadLocalAsset(string assetUri)
        {
            throw new NotImplementedException();
        }

        private int _vertexCountX;
        private int _vertexCountY;
        private Vector4[] _vertexBuffer;

        private void curlPage(CurlParameters curlParams)
        {
            float theta = curlParams.Theta;
            float Ay = curlParams.Ay;
            float alpha = curlParams.Alpha;
            float conicContribution = curlParams.ConicContribution;
            // As the user grabs toward the middle-right of the page, curl the
            // paper by deforming it on to a cylinder. The cylinder radius is taken
            // as the endpoint of the cone parameters: for example,
            // cylRadius = R*sin(theta) distance to where R is the the rightmost
            // point on the page, all the way up.
            float cylR = (float)Math.Sqrt(_vertexCountX * _vertexCountX
                              + (_vertexCountY / 2 - Ay) * (_vertexCountY / 2 - Ay));
            float cylRadius = cylR * (float)Math.Sin(theta);
            // Flipping from top corner or bottom corner?
            float posNegOne;
            if (conicContribution > 0)
            {
                // Top corner
                posNegOne = 1.0f;
            }
            else
            {
                // Bottom corner
                posNegOne = -1.0f;
                Ay = -Ay + _vertexCountY;
            }
            conicContribution = Math.Abs(conicContribution);
            for (int j = 0; j < _vertexCountY; j++)
            {
                for (int i = 0; i < _vertexCountX - 1; i++)
                {
                    float x = (float)i;
                    float y = (float)j;
                    float z = 0;
                    float coneX = x;
                    float coneY = y;
                    float coneZ = z;
                    {
                        // Compute conical parameters and deform
                        float R = (float)Math.Sqrt(x * x + (y - Ay) * (y - Ay));
                        float r = R * (float)Math.Sin(theta);
                        float beta = (float)Math.Asin(x / R) / (float)Math.Sin(theta);
                        coneX = r * (float)Math.Sin(beta);
                        coneY = R + posNegOne * Ay - r * (1 - (float)Math.Cos(beta)) * (float)Math.Sin(theta);
                        coneZ = r * (1 - (float)Math.Cos(beta)) * (float)Math.Cos(theta);
                        // Then rotate by alpha about the y axis
                        coneX = coneX * (float)Math.Cos(alpha) - coneZ * (float)Math.Sin(alpha);
                        coneZ = coneX * (float)Math.Sin(alpha) + coneZ * (float)Math.Cos(alpha);
                    }
                    float cylX = x;
                    float cylY = y;
                    float cylZ = z;
                    {
                        float beta = cylX / cylRadius;
                        // Rotate (0,0,0) by beta around line given by x = 0, z = cylRadius
                        // aka Rotate (0,0,-cylRadius) by beta, then add cylRadius back
                        // to z coordinate
                        cylZ = -cylRadius;
                        cylX = -cylZ * (float)Math.Sin(beta);
                        cylZ = cylZ * (float)Math.Cos(beta);
                        cylZ += cylRadius;
                        // Then rotate by alpha about the y axis
                        cylX = cylX * (float)Math.Cos(alpha) - cylZ * (float)Math.Sin(alpha);
                        cylZ = cylX * (float)Math.Sin(alpha) + cylZ * (float)Math.Cos(alpha);
                    }
                    // Combine cone & cylinder results
                    x = conicContribution * coneX + (1 - conicContribution) * cylX;
                    y = conicContribution * coneY + (1 - conicContribution) * cylY;
                    z = conicContribution * coneZ + (1 - conicContribution) * cylZ;
                    _vertexBuffer[j * _vertexCountX + i].X = x;
                    _vertexBuffer[j * _vertexCountX + i].Y = y;
                    _vertexBuffer[j * _vertexCountX + i].Z = z;
                    _vertexBuffer[j * _vertexCountX + i].W = 1.0f;
                    _vertexBuffer[j * _vertexCountX + i + 1].X = 1.0f;
                    _vertexBuffer[j * _vertexCountX + i + 1].Y = 1.0f;
                    _vertexBuffer[j * _vertexCountX + i + 1].Z = 1.0f;
                    _vertexBuffer[j * _vertexCountX + i + 1].W = 1.0f;

                }
            }


        }


        private const float _maxCurlDistance = 5.0f;
        CurlParameters _nextCurlParams = new CurlParameters();
        CurlParameters _currentCurlParams = new CurlParameters();

        private void RenderCurl()
        {
            // Read state under a lock
              CurlParameters nextCurlParams;
              CurlParameters currentCurlParams;
              bool userCurl;
              bool autoCurl;
              //LOCK(_mutex)
              //{
                nextCurlParams = _nextCurlParams;
                currentCurlParams = _currentCurlParams;
                userCurl = true;// _userCurl;
                autoCurl = false;// _autoCurl;
              //}
              // Smooth going from currentCurlParams to nextCurlParams
              CurlParameters curl = new CurlParameters();
              float dt = nextCurlParams.Theta - currentCurlParams.Theta;
              float da = nextCurlParams.Ay    - currentCurlParams.Ay;
              float dr = nextCurlParams.Alpha - currentCurlParams.Alpha;
              float dc = nextCurlParams.ConicContribution -
                currentCurlParams.ConicContribution;
              float distance = (float)Math.Sqrt(dt * dt + da * da + dr * dr + dc * dc);
              if (distance < _maxCurlDistance)
              {
                curl = nextCurlParams;
              }
              else
              {
                  float scale = _maxCurlDistance / distance;
                curl.Theta = currentCurlParams.Theta + scale * dt;
                curl.Ay =  currentCurlParams.Ay  + scale * da;
                curl.Alpha = currentCurlParams.Alpha + scale * dr;
                curl.ConicContribution = 
                  currentCurlParams.ConicContribution + scale * dc;
              }
              // Deform the vertex buffer
              if (userCurl || autoCurl)
              {
                //LOCK(_mutex)
                //{
                  _currentCurlParams = curl;
                //}
                curlPage(curl);

                  
              }
              // Continue (or stop) uncurling
              if (autoCurl)
              {
                //this->continueAutoCurl();
              }
            
            
        }


        private void startUserCurl(float x, float y)
        {
             CurlParameters curl = computeCurlParameters(x, y);
              //LOCK(_mutex)
              //{
                // Set curl state, to be consumed by onRender()
                _nextCurlParams = curl;
                //_userCurl = true;
                //_autoCurl = false;
              //}
        }

        private CurlParameters computeCurlParameters(float x, float y)
        {
            float theta, ay, alpha;
              if (x > 0.95f)
              {
                theta = STRAIGHT_LINE(1.0f,  90.0f, 0.95f, 60.0f, x);
                ay    = STRAIGHT_LINE(1.0f, -20.0f, 0.95f, -5.0f, x);
                alpha = 0.0f;
              }
              else if (x > 0.8333f)
              {
                theta = STRAIGHT_LINE(0.95f,  60.0f, 0.8333f, 55.0f, x);
                ay    = STRAIGHT_LINE(0.95f, -5.0f,  0.8333f, -4.0f, x);
                alpha = STRAIGHT_LINE(0.95f,  0.0f,  0.8333f, 13.0f, x);
              }
              else if (x > 0.3333f)
              {
                theta = STRAIGHT_LINE(0.8333f, 55.0f, 0.3333f,  45.0f, x);
                ay    = STRAIGHT_LINE(0.8333f, -4.0f, 0.3333f, -10.0f, x);
                alpha = STRAIGHT_LINE(0.8333f, 13.0f, 0.3333f,  35.0f, x);
              }
              else if (x > 0.1666f)
              {
                theta = STRAIGHT_LINE(0.3333f,  45.0f, 0.1666f,  25.0f, x);
                ay    = STRAIGHT_LINE(0.3333f, -10.0f, 0.1666f, -30.0f, x);
                alpha = STRAIGHT_LINE(0.3333f,  35.0f, 0.1666f,  60.0f, x);
              }
              else
              {
                theta = STRAIGHT_LINE(0.1666f,  25.0f, 0.0f,  20.0f, x);
                ay    = STRAIGHT_LINE(0.1666f, -30.0f, 0.0f, -40.0f, x);
                alpha = STRAIGHT_LINE(0.1666f,  60.0f, 0.0f,  95.0f, x);
              }
              CurlParameters cp = new CurlParameters(theta, ay, alpha, y);
              return cp;
            }
            

        private float STRAIGHT_LINE(float x1,float y1,float x2,float y2,float x)
        {
 	        return (((y2 - y1) / (x2 - x1)) * (x - x1) + y1);
        }


     
    }

    public class Particle2
    {
        public double Opacity;
        public Vector2 Position;
        public Vector2 Velocity;
        public Vector2 Size;
        public double TimeToLive;
        public double Elapsed;
        public bool IsAlive;

    }
}
