/*
   Copyright 2012 Harri Smatt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package fi.harism.curl;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * OpenGL ES View.
 * 
 * @author harism
 */
public class CurlView extends GLSurfaceView implements View.OnTouchListener,
		CurlRenderer.Observer {

	// Curl state. We are flipping none, left or right page.
	private static final int CURL_LEFT = 1;
	private static final int CURL_NONE = 0;
	private static final int CURL_RIGHT = 2;

	// Constants for mAnimationTargetEvent.
	private static final int SET_CURL_TO_LEFT = 1;
	private static final int SET_CURL_TO_RIGHT = 2;

	// Shows one page at the center of view.
	public static final int SHOW_ONE_PAGE = 1;
	// Shows two pages side by side.
	public static final int SHOW_TWO_PAGES = 2;

	private boolean mAllowLastPageCurl = true;

	private boolean mAnimate = false;
	private long mAnimationDurationTime = 300;
	private PointF mAnimationSource = new PointF();
	private long mAnimationStartTime;
	private PointF mAnimationTarget = new PointF();
	private int mAnimationTargetEvent;

	private PointF mCurlDir = new PointF();

	private PointF mCurlPos = new PointF();
	private int mCurlState = CURL_NONE;
	// Current bitmap index. This is always showed as front of right page.
	private int mCurrentIndex = 0;

	// Start position for dragging.
	private PointF mDragStartPos = new PointF();

	private boolean mEnableTouchPressure = false;
	// Bitmap size. These are updated from renderer once it's initialized.
	private int mPageBitmapHeight = -1;

	private int mPageBitmapWidth = -1;
	// Page meshes. Left and right meshes are 'static' while curl is used to
	// show page flipping.
	private CurlMesh mPageCurl;

	private CurlMesh mPageLeft;
	private PageProvider mPageProvider;
	private CurlMesh mPageRight;

	private PointerPosition mPointerPos = new PointerPosition();

	private CurlRenderer mRenderer;
	private boolean mRenderLeftPage = true;
	private SizeChangedObserver mSizeChangedObserver;

	// One page is the default.
	private int mViewMode = SHOW_ONE_PAGE;

	public static final int BIND_LEFT = 0;
	public static final int BIND_RIGHT = 1;
	public static final int BIND_TOP = 2;
	public static final int BIND_BOTTOM = 3;

	private int mBindPosition = BIND_LEFT;

	private float mZoomLevel = 1.0f;
	private float mPanX = 0.0f;
	private float mPanY = 0.0f;
	// aLINK add: Use only the maximum size for rendering the bitmaps
	public boolean mUseCachedBitmaps = true;
	// end

	/**
	 * Default constructor.
	 */
	public CurlView(Context ctx) {
		super(ctx);
		init(ctx);
	}

	/**
	 * Default constructor.
	 */
	public CurlView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init(ctx);
	}

	/**
	 * Default constructor.
	 */
	public CurlView(Context ctx, AttributeSet attrs, int defStyle) {
		this(ctx, attrs);
	}

	/**
	 * Get current page index. Page indices are zero based values presenting
	 * page being shown on right side of the book.
	 */
	public int getCurrentIndex() {
		return mCurrentIndex;
	}

	/**
	 * Initialize method.
	 */
	private void init(Context ctx) {
		mRenderer = new CurlRenderer(this);

		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setOnTouchListener(this);

		// Even though left and right pages are static we have to allocate room
		// for curl on them too as we are switching meshes. Another way would be
		// to swap texture ids only.
		mPageLeft = new CurlMesh(10);
		mPageRight = new CurlMesh(10);
		mPageCurl = new CurlMesh(10);
		mPageLeft.setFlipTexture(true);
		mPageRight.setFlipTexture(false);
	}

	@Override
	public void onDrawFrame() {
		// We are not animating.
		if (mAnimate == false) {
			return;
		}

		long currentTime = System.currentTimeMillis();
		// If animation is done.
		if (currentTime >= mAnimationStartTime + mAnimationDurationTime) {
			if (mAnimationTargetEvent == SET_CURL_TO_RIGHT) {
				// Switch curled page to right.
				CurlMesh right = mPageCurl;
				CurlMesh curl = mPageRight;
				right.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
				right.setFlipTexture(false);
				right.reset();
				mRenderer.removeCurlMesh(curl);
				mPageCurl = curl;
				mPageRight = right;
				// If we were curling left page update current index.
				if (mCurlState == CURL_LEFT) {
					--mCurrentIndex;
					
					mPageProvider.indexChanged(mCurrentIndex);
				}
			} else if (mAnimationTargetEvent == SET_CURL_TO_LEFT) {
				// Switch curled page to left.
				CurlMesh left = mPageCurl;
				CurlMesh curl = mPageLeft;
				left.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
				left.setFlipTexture(true);
				left.reset();
				mRenderer.removeCurlMesh(curl);
				if (!mRenderLeftPage) {
					mRenderer.removeCurlMesh(left);
				}
				mPageCurl = curl;
				mPageLeft = left;
				// If we were curling right page update current index.
				if (mCurlState == CURL_RIGHT) {
					++mCurrentIndex;
					
					mPageProvider.indexChanged(mCurrentIndex);
				}
			}
			mCurlState = CURL_NONE;
			mAnimate = false;
			requestRender();
		} else {
			mPointerPos.mPos.set(mAnimationSource);
			float t = 1f - ((float) (currentTime - mAnimationStartTime) / mAnimationDurationTime);
			t = 1f - (t * t * t * (3 - 2 * t));
			mPointerPos.mPos.x += (mAnimationTarget.x - mAnimationSource.x) * t;
			mPointerPos.mPos.y += (mAnimationTarget.y - mAnimationSource.y) * t;
			updateCurlPos(mPointerPos);
		}
		
	}

	@Override
	public void onPageSizeChanged(int width, int height) {
		// aLINK edit: Use only the maximum size for rendering the bitmaps
		if ((width > mPageBitmapWidth) || (height > mPageBitmapHeight)) {
			mPageBitmapWidth = width;
			mPageBitmapHeight = height;
			mUseCachedBitmaps = false;
		}
		// end
		setZoomLevel(mZoomLevel);
		setPans(mPanX, mPanY);
		updatePages();
		requestRender();
	}

	@Override
	public void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		requestRender();
		if (mSizeChangedObserver != null) {
			mSizeChangedObserver.onSizeChanged(w, h);
		}
	}

	@Override
	public void onSurfaceCreated() {
		// In case surface is recreated, let page meshes drop allocated texture
		// ids and ask for new ones. There's no need to set textures here as
		// onPageSizeChanged should be called later on.
		mPageLeft.resetTexture();
		mPageRight.resetTexture();
		mPageCurl.resetTexture();
	}
	
	public boolean onePageDrawnNotCurling() {
		// Check if left page is drawn
		boolean leftPageIsDrawn = (mCurrentIndex > 1) || (mCurrentIndex == 1 && mCurlState != CURL_LEFT);
		boolean rightPageIsDrawn = (mCurrentIndex < mPageProvider.getPageCount()-1)
										|| (mCurrentIndex ==  mPageProvider.getPageCount()-1 && mCurlState != CURL_RIGHT);

		return leftPageIsDrawn || rightPageIsDrawn;
	}
	
	public RectF getBorderRect(){
		RectF lRect =  mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);
		RectF rect = new RectF(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));

		boolean leftPageIsDrawn = (mCurrentIndex > 1) || (mCurrentIndex == 1 && mCurlState != CURL_LEFT);
		boolean rightPageIsDrawn = (mCurrentIndex < mPageProvider.getPageCount()-1)
										|| (mCurrentIndex ==  mPageProvider.getPageCount()-1 && mCurlState != CURL_RIGHT);
		
		if (mRenderLeftPage && leftPageIsDrawn) {
			switch (mBindPosition) {
				case BIND_LEFT:
					rect.left = lRect.left;
					break;
				case BIND_RIGHT:
					rect.right = lRect.right;
					break;
				case BIND_TOP:
					rect.top = lRect.top;
					break;
				case BIND_BOTTOM:
					rect.bottom = lRect.bottom;
					break;
			}
		}

		if (mAllowLastPageCurl && !rightPageIsDrawn) {
			if (mRenderLeftPage) {
				rect.set(lRect);
			} else {
				return null;
			}
		}

		return rect;
	}
	
	/**
	 * Checks if the curl view is hit on the coordinates
	 * 
	 * @param x
	 * @param y
	 * @return Is hit
	 */
	public boolean isViewHit(float x, float y) {
		PointF pos = new PointF(x, y);
		mRenderer.translate(pos);
		
		pos.x /= mZoomLevel;
		pos.y /= mZoomLevel;

		RectF rect = getBorderRect();
		if ((pos.y > rect.top) || (pos.y < rect.bottom)) {
			return false;
		}
		if ((pos.x > rect.right) || (pos.x < rect.left)) {
			return false;
		}

		return true;
	}

	/**
	 * Prevent touch out of margin
	 * 
	 * @param rightRect
	 * @param leftRect
	 * @return Will prevent touch
	 */
	private boolean preventTouch() {
		RectF rect = getBorderRect();

		//Prevent touch out of margin
		if ((mDragStartPos.y > rect.top)
				|| (mDragStartPos.y < rect.bottom)) {
			return true;
		}
		if ((mDragStartPos.x > rect.right)
				|| (mDragStartPos.x < rect.left)) {
			return true;
		}

		return false;
	}

	@Override
	public boolean onTouch(View view, MotionEvent me) {
		// No dragging during animation at the moment.
		// TODO: Stop animation on touch event and return to drag mode.
		if (mAnimate || mPageProvider == null) {
			return false;
		}

		// We need page rects quite extensively so get them for later use.
		RectF rightRect = new RectF(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
		RectF leftRect = new RectF(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));

		// Store pointer position.
		mPointerPos.mPos.set(me.getX(), me.getY());
		mRenderer.translate(mPointerPos.mPos);

		rightRect.left *= mZoomLevel;
		rightRect.right *= mZoomLevel;
		rightRect.top *= mZoomLevel;
		rightRect.bottom *= mZoomLevel;
		
		leftRect.left *= mZoomLevel;
		leftRect.right *= mZoomLevel;
		leftRect.top *= mZoomLevel;
		leftRect.bottom *= mZoomLevel;

		mPointerPos.mPos.x /= mZoomLevel;
		mPointerPos.mPos.y /= mZoomLevel;

		if (mEnableTouchPressure) {
			mPointerPos.mPressure = me.getPressure();
		} else {
			mPointerPos.mPressure = 0.8f;
		}

		switch (me.getAction()) {
		case MotionEvent.ACTION_DOWN: {

			// Once we receive pointer down event its position is mapped to
			// right or left edge of page and that'll be the position from where
			// user is holding the paper to make curl happen.
			mDragStartPos.set(mPointerPos.mPos);
			
			if (preventTouch()) {
				return false;
			}

			// Then we have to make decisions for the user whether curl is going
			// to happen from left or right, and on which page.
			if (mViewMode == SHOW_TWO_PAGES) {
				if (mBindPosition == BIND_LEFT) {
					// If we have an open book and pointer is on the left from right
					// page we'll mark drag position to left edge of left page.
					// Additionally checking mCurrentIndex is higher than zero tells
					// us there is a visible page at all.
					if (mDragStartPos.x < rightRect.left && mCurrentIndex > 0) {
						mDragStartPos.x = leftRect.left;
						startCurl(CURL_LEFT);
					}
					// Otherwise check pointer is on right page's side.
					else if (mDragStartPos.x >= rightRect.left
							&& mCurrentIndex < mPageProvider.getPageCount()) {
						mDragStartPos.x = rightRect.right;
						if (!mAllowLastPageCurl
								&& mCurrentIndex >= mPageProvider.getPageCount() - 1) {
							return true;
						}
						startCurl(CURL_RIGHT);
					}
				}

				if (mBindPosition == BIND_RIGHT) {
					if (mDragStartPos.x > rightRect.right && mCurrentIndex > 0) {
						mDragStartPos.x = leftRect.right;
						startCurl(CURL_LEFT);
					} else if (mDragStartPos.x <= rightRect.right
							&& mCurrentIndex < mPageProvider.getPageCount()) {
						mDragStartPos.x = rightRect.left;
						if (!mAllowLastPageCurl
								&& mCurrentIndex >= mPageProvider.getPageCount() - 1) {
							return true;
						}
						startCurl(CURL_RIGHT);
					}
				}

				if (mBindPosition == BIND_TOP) {
					if (mDragStartPos.y > rightRect.top && mCurrentIndex > 0) {
						mDragStartPos.y = leftRect.top;
						startCurl(CURL_LEFT);
					}
					// Otherwise check pointer is on right page's side.
					else if (mDragStartPos.y <= rightRect.top
							&& mCurrentIndex < mPageProvider.getPageCount()) {
						mDragStartPos.y = rightRect.bottom;
						if (!mAllowLastPageCurl
								&& mCurrentIndex >= mPageProvider.getPageCount() - 1) {
							return true;
						}
						startCurl(CURL_RIGHT);
					}
					
				}

				if (mBindPosition == BIND_BOTTOM) {
					if (mDragStartPos.y < rightRect.bottom && mCurrentIndex > 0) {
						mDragStartPos.y = leftRect.bottom;
						startCurl(CURL_LEFT);
					}
					// Otherwise check pointer is on right page's side.
					else if (mDragStartPos.y >= rightRect.bottom
							&& mCurrentIndex < mPageProvider.getPageCount()) {
						mDragStartPos.y = rightRect.top;
						if (!mAllowLastPageCurl
								&& mCurrentIndex >= mPageProvider.getPageCount() - 1) {
							return true;
						}
						startCurl(CURL_RIGHT);
					}
					
				}

			} else if (mViewMode == SHOW_ONE_PAGE) {
				if (mBindPosition == BIND_LEFT) {
					float halfX = (rightRect.right + rightRect.left) / 2;
					if (mDragStartPos.x < halfX && mCurrentIndex > 0) {
						mDragStartPos.x = rightRect.left;
						startCurl(CURL_LEFT);
					} else if (mDragStartPos.x >= halfX
							&& mCurrentIndex < mPageProvider.getPageCount()) {
						mDragStartPos.x = rightRect.right;
						if (!mAllowLastPageCurl
								&& mCurrentIndex >= mPageProvider.getPageCount() - 1) {
							return true;
						}
						startCurl(CURL_RIGHT);
					}
				}

				if (mBindPosition == BIND_RIGHT) {
					float halfX = (rightRect.right + rightRect.left) / 2;
					if (mDragStartPos.x > halfX && mCurrentIndex > 0) {
						mDragStartPos.x = rightRect.right;
						startCurl(CURL_LEFT);
					} else if (mDragStartPos.x <= halfX
							&& mCurrentIndex < mPageProvider.getPageCount()) {
						mDragStartPos.x = rightRect.left;
						if (!mAllowLastPageCurl
								&& mCurrentIndex >= mPageProvider.getPageCount() - 1) {
							return true;
						}
						startCurl(CURL_RIGHT);
					}
				}

				if (mBindPosition == BIND_TOP) {
					float halfY = (rightRect.top + rightRect.bottom) / 2;
					if (mDragStartPos.y > halfY && mCurrentIndex > 0) {
						mDragStartPos.y = rightRect.top;
						startCurl(CURL_LEFT);
					} else if (mDragStartPos.y <= halfY
							&& mCurrentIndex < mPageProvider.getPageCount()) {
						mDragStartPos.y = rightRect.bottom;
						if (!mAllowLastPageCurl
								&& mCurrentIndex >= mPageProvider.getPageCount() - 1) {
							return true;
						}
						startCurl(CURL_RIGHT);
					}
				}

				if (mBindPosition == BIND_BOTTOM) {
					float halfY = (rightRect.top + rightRect.bottom) / 2;
					if (mDragStartPos.y < halfY && mCurrentIndex > 0) {
						mDragStartPos.y = rightRect.bottom;
						startCurl(CURL_LEFT);
					} else if (mDragStartPos.y >= halfY
							&& mCurrentIndex < mPageProvider.getPageCount()) {
						mDragStartPos.y = rightRect.top;
						if (!mAllowLastPageCurl
								&& mCurrentIndex >= mPageProvider.getPageCount() - 1) {
							return true;
						}
						startCurl(CURL_RIGHT);
					}
				}
			}
			// If we have are in curl state, let this case clause flow through
			// to next one. We have pointer position and drag position defined
			// and this will create first render request given these points.
			if (mCurlState == CURL_NONE) {
				return true;
			}
		}
		case MotionEvent.ACTION_MOVE: {
			updateCurlPos(mPointerPos);
			break;
		}
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			if (mCurlState == CURL_LEFT || mCurlState == CURL_RIGHT) {
				// Animation source is the point from where animation starts.
				// Also it's handled in a way we actually simulate touch events
				// meaning the output is exactly the same as if user drags the
				// page to other side. While not producing the best looking
				// result (which is easier done by altering curl position and/or
				// direction directly), this is done in a hope it made code a
				// bit more readable and easier to maintain.
				mAnimationSource.set(mPointerPos.mPos);
				mAnimationStartTime = System.currentTimeMillis();
				
				if (mBindPosition == BIND_LEFT) {
					// Given the explanation, here we decide whether to simulate
					// drag to left or right end.
					if (((mViewMode == SHOW_ONE_PAGE && mPointerPos.mPos.x > (rightRect.left + rightRect.right) / 2)
							|| mViewMode == SHOW_TWO_PAGES
							&& mPointerPos.mPos.x > rightRect.left)) {
						// On right side target is always right page's right border.
						mAnimationTarget.set(mDragStartPos);
						mAnimationTarget.x = mRenderer
								.getPageRect(CurlRenderer.PAGE_RIGHT).right;
						mAnimationTargetEvent = SET_CURL_TO_RIGHT;
					} else {
						// On left side target depends on visible pages.
						mAnimationTarget.set(mDragStartPos);
						if (mCurlState == CURL_RIGHT || mViewMode == SHOW_TWO_PAGES) {
							mAnimationTarget.x = leftRect.left;
						} else {
							mAnimationTarget.x = rightRect.left;
						}
						mAnimationTargetEvent = SET_CURL_TO_LEFT;
					}
				}

				if (mBindPosition == BIND_RIGHT) {
					// Given the explanation, here we decide whether to simulate
					// drag to left or right end.
					if (((mViewMode == SHOW_ONE_PAGE && mPointerPos.mPos.x < (rightRect.left + rightRect.right) / 2)
							|| mViewMode == SHOW_TWO_PAGES
							&& mPointerPos.mPos.x < rightRect.right)) {
						// On left side target is always right page's left border.
						mAnimationTarget.set(mDragStartPos);
						mAnimationTarget.x = mRenderer
								.getPageRect(CurlRenderer.PAGE_RIGHT).left;
						mAnimationTargetEvent = SET_CURL_TO_RIGHT;
					} else {
						// On right side target depends on visible pages.
						mAnimationTarget.set(mDragStartPos);
						if (mCurlState == CURL_RIGHT || mViewMode == SHOW_TWO_PAGES) {
							mAnimationTarget.x = leftRect.right;
						} else {
							mAnimationTarget.x = rightRect.right;
						}
						mAnimationTargetEvent = SET_CURL_TO_LEFT;
					}
				}

				if (mBindPosition == BIND_TOP) {
					if (((mViewMode == SHOW_ONE_PAGE && mPointerPos.mPos.y < (rightRect.top + rightRect.bottom) / 2)
							|| mViewMode == SHOW_TWO_PAGES
							&& mPointerPos.mPos.y < rightRect.top)) {
						// On right side target is always right page's right border.
						mAnimationTarget.set(mDragStartPos);
						mAnimationTarget.y = mRenderer
								.getPageRect(CurlRenderer.PAGE_RIGHT).bottom;
						mAnimationTargetEvent = SET_CURL_TO_RIGHT;
					} else {
						// On left side target depends on visible pages.
						mAnimationTarget.set(mDragStartPos);
						if (mCurlState == CURL_RIGHT || mViewMode == SHOW_TWO_PAGES) {
							mAnimationTarget.y = leftRect.top;
						} else {
							mAnimationTarget.y = rightRect.top;
						}
						mAnimationTargetEvent = SET_CURL_TO_LEFT;
					}
				}

				if (mBindPosition == BIND_BOTTOM) {
					if (((mViewMode == SHOW_ONE_PAGE && mPointerPos.mPos.y > (rightRect.top + rightRect.bottom) / 2)
							|| mViewMode == SHOW_TWO_PAGES
							&& mPointerPos.mPos.y > rightRect.bottom)) {
						// On right side target is always right page's right border.
						mAnimationTarget.set(mDragStartPos);
						mAnimationTarget.y = mRenderer
								.getPageRect(CurlRenderer.PAGE_RIGHT).top;
						mAnimationTargetEvent = SET_CURL_TO_RIGHT;
					} else {
						// On left side target depends on visible pages.
						mAnimationTarget.set(mDragStartPos);
						if (mCurlState == CURL_RIGHT || mViewMode == SHOW_TWO_PAGES) {
							mAnimationTarget.y = leftRect.bottom;
						} else {
							mAnimationTarget.y = rightRect.bottom;
						}
						mAnimationTargetEvent = SET_CURL_TO_LEFT;
					}
				}

				mAnimate = true;
				requestRender();
			}
			break;
		}
		}

		return true;
	}

	/**
	 * Is last page curl allowed
	 */
	public boolean getAllowLastPageCurl() {
		return mAllowLastPageCurl;
	}

	/**
	 * Allow the last page to curl.
	 */
	public void setAllowLastPageCurl(boolean allowLastPageCurl) {
		mAllowLastPageCurl = allowLastPageCurl;
	}

	/**
	 * Sets background color - or OpenGL clear color to be more precise. Color
	 * is a 32bit value consisting of 0xAARRGGBB and is extracted using
	 * android.graphics.Color eventually.
	 */
	@Override
	public void setBackgroundColor(int color) {
		mRenderer.setBackgroundColor(color);
		requestRender();
	}

	/**
	 * Sets mPageCurl curl position.
	 */
	private void setCurlPos(PointF curlPos, PointF curlDir, double radius) {

		// First reposition curl so that page doesn't 'rip off' from book.
		if (mCurlState == CURL_RIGHT
				|| (mCurlState == CURL_LEFT && mViewMode == SHOW_ONE_PAGE)) {

			if (mBindPosition == BIND_LEFT) {
				RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
				if (curlPos.x >= pageRect.right) {
					mPageCurl.reset();
					requestRender();
					return;
				}
				if (curlPos.x < pageRect.left) {
					curlPos.x = pageRect.left;
				}
				if (curlDir.y != 0) {
					float diffX = curlPos.x - pageRect.left;
					float leftY = curlPos.y + (diffX * curlDir.x / curlDir.y);
					if (curlDir.y < 0 && leftY < pageRect.top) {
						curlDir.x = curlPos.y - pageRect.top;
						curlDir.y = pageRect.left - curlPos.x;
					} else if (curlDir.y > 0 && leftY > pageRect.bottom) {
						curlDir.x = pageRect.bottom - curlPos.y;
						curlDir.y = curlPos.x - pageRect.left;
					}
				}
			}

			if (mBindPosition == BIND_RIGHT) {
				RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
				if (curlPos.x <= pageRect.left) {
					mPageCurl.reset();
					requestRender();
					return;
				}
				if (curlPos.x > pageRect.right) {
					curlPos.x = pageRect.right;
				}
				if (curlDir.y != 0) {
					float diffX = curlPos.x - pageRect.right;
					float leftY = curlPos.y + (diffX * curlDir.x / curlDir.y);
					if (curlDir.y < 0 && leftY < pageRect.top) {
						curlDir.x = pageRect.top - curlPos.y;
						curlDir.y = curlPos.x - pageRect.right;
					} else if (curlDir.y > 0 && leftY > pageRect.bottom) {
						curlDir.x = curlPos.y - pageRect.bottom;
						curlDir.y = pageRect.right - curlPos.x;
					}
				}
			}

			if (mBindPosition == BIND_TOP) {
				RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
				if (curlPos.y <= pageRect.bottom) {
					mPageCurl.reset();
					requestRender();
					return;
				}
				if (curlPos.y > pageRect.top) {
					curlPos.y = pageRect.top;
				}
				if (curlDir.x != 0) {
					float diffY = curlPos.y - pageRect.top;
					float topX = curlPos.x + (diffY * curlDir.y / curlDir.x);
					if (curlDir.x > 0 && topX > pageRect.left) {
						curlDir.y = curlPos.x - pageRect.left;
						curlDir.x = pageRect.top - curlPos.y;
					} else if (curlDir.x < 0 && topX < pageRect.right) {
						curlDir.y = pageRect.right - curlPos.x;
						curlDir.x = curlPos.y - pageRect.top;
					}
				}
			}

			if (mBindPosition == BIND_BOTTOM) {
				RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
				if (curlPos.y >= pageRect.top) {
					mPageCurl.reset();
					requestRender();
					return;
				}
				if (curlPos.y < pageRect.bottom) {
					curlPos.y = pageRect.bottom;
				}
				if (curlDir.x != 0) {
					float diffY = curlPos.y - pageRect.bottom;
					float topX = curlPos.x + (diffY * curlDir.y / curlDir.x);
					if (curlDir.x > 0 && topX > pageRect.left) {
						curlDir.y = pageRect.left - curlPos.x;
						curlDir.x = curlPos.y - pageRect.bottom;
					} else if (curlDir.x < 0 && topX < pageRect.right) {
						curlDir.y = curlPos.x - pageRect.right;
						curlDir.x = pageRect.bottom - curlPos.y;
					}
				}
			}
		} else if (mCurlState == CURL_LEFT) {
			if (mBindPosition == BIND_LEFT) {
				RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);
				if (curlPos.x <= pageRect.left) {
					mPageCurl.reset();
					requestRender();
					return;
				}
				if (curlPos.x > pageRect.right) {
					curlPos.x = pageRect.right;
				}
				if (curlDir.y != 0) {
					float diffX = curlPos.x - pageRect.right;
					float rightY = curlPos.y + (diffX * curlDir.x / curlDir.y);
					if (curlDir.y < 0 && rightY < pageRect.top) {
						curlDir.x = pageRect.top - curlPos.y;
						curlDir.y = curlPos.x - pageRect.right;
					} else if (curlDir.y > 0 && rightY > pageRect.bottom) {
						curlDir.x = curlPos.y - pageRect.bottom;
						curlDir.y = pageRect.right - curlPos.x;
					}
				}
			}

			if (mBindPosition == BIND_RIGHT) {
				RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);
				if (curlPos.x >= pageRect.right) {
					mPageCurl.reset();
					requestRender();
					return;
				}
				if (curlPos.x < pageRect.left) {
					curlPos.x = pageRect.left;
				}
				if (curlDir.y != 0) {
					float diffX = curlPos.x - pageRect.left;
					float rightY = curlPos.y + (diffX * curlDir.x / curlDir.y);
					if (curlDir.y < 0 && rightY < pageRect.top) {
						curlDir.x = curlPos.y - pageRect.top;
						curlDir.y = pageRect.left - curlPos.x;
					} else if (curlDir.y > 0 && rightY > pageRect.bottom) {
						curlDir.x = pageRect.bottom - curlPos.y;
						curlDir.y = curlPos.x - pageRect.left;
					}
				}
			}

			if (mBindPosition == BIND_TOP) {
				RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);
				if (curlPos.y >= pageRect.top) {
					mPageCurl.reset();
					requestRender();
					return;
				}
				if (curlPos.y < pageRect.bottom) {
					curlPos.y = pageRect.bottom;
				}
				if (curlDir.x != 0) {
					float diffY = curlPos.y - pageRect.bottom;
					float bottomX = curlPos.x + (diffY * curlDir.y / curlDir.x);
					if (curlDir.x > 0 && bottomX > pageRect.left) {
						curlDir.y = pageRect.left - curlPos.x;
						curlDir.x = curlPos.y - pageRect.bottom;
					} else if (curlDir.x < 0 && bottomX < pageRect.right) {
						curlDir.y = curlPos.x - pageRect.right;
						curlDir.x = pageRect.bottom - curlPos.y;
					}
				}
			}

			if (mBindPosition == BIND_BOTTOM) {
				RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);
				if (curlPos.y <= pageRect.bottom) {
					mPageCurl.reset();
					requestRender();
					return;
				}
				if (curlPos.y > pageRect.top) {
					curlPos.y = pageRect.top;
				}
				if (curlDir.x != 0) {
					float diffY = curlPos.y - pageRect.top;
					float bottomX = curlPos.x + (diffY * curlDir.y / curlDir.x);
					if (curlDir.x > 0 && bottomX > pageRect.left) {
						curlDir.y = curlPos.x - pageRect.left;
						curlDir.x = pageRect.top - curlPos.y;
					} else if (curlDir.x < 0 && bottomX < pageRect.right) {
						curlDir.y = pageRect.right - curlPos.x;
						curlDir.x = curlPos.y - pageRect.top;
					}
				}
			}
		}

		// Finally normalize direction vector and do rendering.
		double dist = Math.sqrt(curlDir.x * curlDir.x + curlDir.y * curlDir.y);
		if (dist != 0) {
			curlDir.x /= dist;
			curlDir.y /= dist;
			mPageCurl.curl(curlPos, curlDir, radius);
		} else {
			mPageCurl.reset();
		}

		requestRender();
	}

	/**
	 * Set current page index. Page indices are zero based values presenting
	 * page being shown on right side of the book. E.g if you set value to 4;
	 * right side front facing bitmap will be with index 4, back facing 5 and
	 * for left side page index 3 is front facing, and index 2 back facing (once
	 * page is on left side it's flipped over).
	 * 
	 * Current index is rounded to closest value divisible with 2.
	 */
	public void setCurrentIndex(int index) {
		if (mPageProvider == null || index < 0) {
			mCurrentIndex = 0;
		} else {
			if (mAllowLastPageCurl) {
				mCurrentIndex = Math.min(index, mPageProvider.getPageCount());
			} else {
				mCurrentIndex = Math.min(index,
						mPageProvider.getPageCount() - 1);
			}
		}
		updatePages();
		requestRender();
	}

	/**
	 * If set to true, touch event pressure information is used to adjust curl
	 * radius. The more you press, the flatter the curl becomes. This is
	 * somewhat experimental and results may vary significantly between devices.
	 * On emulator pressure information seems to be flat 1.0f which is maximum
	 * value and therefore not very much of use.
	 */
	public void setEnableTouchPressure(boolean enableTouchPressure) {
		mEnableTouchPressure = enableTouchPressure;
	}

	/**
	 * Set margins (or padding). Note: margins are proportional. Meaning a value
	 * of .1f will produce a 10% margin.
	 */
	public void setMargins(float left, float top, float right, float bottom) {
		mRenderer.setMargins(left, top, right, bottom);
	}

	/**
	 * Update/set page provider.
	 */
	public void setPageProvider(PageProvider pageProvider) {
		mPageProvider = pageProvider;
		mCurrentIndex = 0;
		updatePages();
		requestRender();
	}

	/**
	 * Setter for whether left side page is rendered. This is useful mostly for
	 * situations where right (main) page is aligned to left side of screen and
	 * left page is not visible anyway.
	 */
	public void setRenderLeftPage(boolean renderLeftPage) {
		mRenderLeftPage = renderLeftPage;
	}

	/**
	 * Sets SizeChangedObserver for this View. Call back method is called from
	 * this View's onSizeChanged method.
	 */
	public void setSizeChangedObserver(SizeChangedObserver observer) {
		mSizeChangedObserver = observer;
	}

	/**
	 * Gets the current view mode
	 */
	public int getViewMode() {
		return mViewMode;
	}

	/**
	 * Sets view mode. Value can be either SHOW_ONE_PAGE or SHOW_TWO_PAGES. In
	 * former case right page is made size of display, and in latter case two
	 * pages are laid on visible area.
	 */
	public void setViewMode(int viewMode) {
		switch (viewMode) {
		case SHOW_ONE_PAGE:
			mViewMode = viewMode;
			mPageLeft.setFlipTexture(true);
			mRenderer.setViewMode(CurlRenderer.SHOW_ONE_PAGE);
			break;
		case SHOW_TWO_PAGES:
			mViewMode = viewMode;
			mPageLeft.setFlipTexture(false);
			mRenderer.setViewMode(CurlRenderer.SHOW_TWO_PAGES);
			break;
		}
	}

	/**
	 * Gets the bind position
	 */
	public int getBindPosition() {
		return mBindPosition;
	}

	/**
	 * Sets the bind position
	 */
	public void setBindPosition(int bindPosition) {
		mBindPosition = bindPosition;
		mRenderer.setBindPosition(bindPosition);
		mPageLeft.setBindPosition(bindPosition);
		mPageRight.setBindPosition(bindPosition);
		mPageCurl.setBindPosition(bindPosition);
	}

	/**
	 * Adjusts the current pan
	 */
	public void adjustPan(float x, float y) {
		mRenderer.tryAdjustPan(x / (float) getWidth(), y / (float) getHeight());
		
		mPanX = mRenderer.getPanX();
		mPanY = mRenderer.getPanY();
	}

	/**
	 * Sets the zoom level of the view
	 */
	public synchronized void setZoomLevel(float zoomLevel) {
		mZoomLevel = zoomLevel;
		mRenderer.setZoomLevel(zoomLevel);
	}

	/**
	 * Sets the drop shadow size if shadow is enabled
	 */
	public void setDropShadowSize(float dropShadowSize) {
		mRenderer.setDropShadowSize(dropShadowSize);
	}

	/**
	 * Sets the current pan of the view
	 */
	public synchronized void setPans(float panX, float panY) {
		mPanX = panX;
		mPanY = panY;
		
		mRenderer.setPans(panX, panY);
	}

	/**
	 * Gets the current horizontal pan value.
	 */
	public float getPanX() {
		return mPanX;
	}

	/**
	 * Gets the current vertical pan value.
	 */
	public float getPanY() {
		return mPanY;
	}

	/**
	 * Switches meshes and loads new bitmaps if available. Updated to support 2
	 * pages in landscape
	 */
	private void startCurl(int page) {
		switch (page) {

		// Once right side page is curled, first right page is assigned into
		// curled page. And if there are more bitmaps available new bitmap is
		// loaded into right side mesh.
		case CURL_RIGHT: {
			// Remove meshes from renderer.
			mRenderer.removeCurlMesh(mPageLeft);
			mRenderer.removeCurlMesh(mPageRight);
			mRenderer.removeCurlMesh(mPageCurl);

			// We are curling right page.
			CurlMesh curl = mPageRight;
			mPageRight = mPageCurl;
			mPageCurl = curl;

			if (mCurrentIndex > 0) {
				mPageLeft.setFlipTexture(true);
				mPageLeft
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
				mPageLeft.reset();
				if (mRenderLeftPage) {
					mRenderer.addCurlMesh(mPageLeft);
				}
			}
			if (mCurrentIndex < mPageProvider.getPageCount() - 1) {
				updatePage(mPageRight.getTexturePage(), mCurrentIndex + 1);
				mPageRight.setRect(mRenderer
						.getPageRect(CurlRenderer.PAGE_RIGHT));
				mPageRight.setFlipTexture(false);
				mPageRight.reset();
				mRenderer.addCurlMesh(mPageRight);
			}

			// Add curled page to renderer.
			mPageCurl.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
			mPageCurl.setFlipTexture(false);
			mPageCurl.reset();
			mRenderer.addCurlMesh(mPageCurl);

			mCurlState = CURL_RIGHT;
			break;
		}

		// On left side curl, left page is assigned to curled page. And if
		// there are more bitmaps available before currentIndex, new bitmap
		// is loaded into left page.
		case CURL_LEFT: {
			// Remove meshes from renderer.
			mRenderer.removeCurlMesh(mPageLeft);
			mRenderer.removeCurlMesh(mPageRight);
			mRenderer.removeCurlMesh(mPageCurl);

			// We are curling left page.
			CurlMesh curl = mPageLeft;
			mPageLeft = mPageCurl;
			mPageCurl = curl;

			if (mCurrentIndex > 1) {
				updatePage(mPageLeft.getTexturePage(), mCurrentIndex - 2);
				mPageLeft.setFlipTexture(true);
				mPageLeft
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
				mPageLeft.reset();
				if (mRenderLeftPage) {
					mRenderer.addCurlMesh(mPageLeft);
				}
			}

			// If there is something to show on right page add it to renderer.
			if (mCurrentIndex < mPageProvider.getPageCount()) {
				mPageRight.setFlipTexture(false);
				mPageRight.setRect(mRenderer
						.getPageRect(CurlRenderer.PAGE_RIGHT));
				mPageRight.reset();
				mRenderer.addCurlMesh(mPageRight);
			}

			// How dragging previous page happens depends on view mode.
			if (mViewMode == SHOW_ONE_PAGE
					|| (mCurlState == CURL_LEFT && mViewMode == SHOW_TWO_PAGES)) {
				mPageCurl.setRect(mRenderer
						.getPageRect(CurlRenderer.PAGE_RIGHT));
				mPageCurl.setFlipTexture(false);
			} else {
				mPageCurl
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
				mPageCurl.setFlipTexture(true);
			}
			mPageCurl.reset();
			mRenderer.addCurlMesh(mPageCurl);

			mCurlState = CURL_LEFT;
			break;
		}

		}
	}

	/**
	 * Updates curl position.
	 */
	private void updateCurlPos(PointerPosition pointerPos) {

		// Default curl radius.
		double radius = mRenderer.getPageRect(CURL_RIGHT).width() / 3;
		if (mBindPosition == BIND_TOP || mBindPosition == BIND_BOTTOM) {
			radius = -mRenderer.getPageRect(CURL_RIGHT).height() / 3;
		}
		// TODO: This is not an optimal solution. Based on feedback received so
		// far; pressure is not very accurate, it may be better not to map
		// coefficient to range [0f, 1f] but something like [.2f, 1f] instead.
		// Leaving it as is until get my hands on a real device. On emulator
		// this doesn't work anyway.
		radius *= Math.max(1f - pointerPos.mPressure, 0f);
		// NOTE: Here we set pointerPos to mCurlPos. It might be a bit confusing
		// later to see e.g "mCurlPos.x - mDragStartPos.x" used. But it's
		// actually pointerPos we are doing calculations against. Why? Simply to
		// optimize code a bit with the cost of making it unreadable. Otherwise
		// we had to this in both of the next if-else branches.
		mCurlPos.set(pointerPos.mPos);

		// If curl happens on right page, or on left page on two page mode,
		// we'll calculate curl position from pointerPos.
		if (mCurlState == CURL_RIGHT
				|| (mCurlState == CURL_LEFT && mViewMode == SHOW_TWO_PAGES)) {

			mCurlDir.x = mCurlPos.x - mDragStartPos.x;
			mCurlDir.y = mCurlPos.y - mDragStartPos.y;
			float dist = (float) Math.sqrt(mCurlDir.x * mCurlDir.x + mCurlDir.y
					* mCurlDir.y);
			
			if (mBindPosition == BIND_LEFT || mBindPosition == BIND_RIGHT) {
				// Adjust curl radius so that if page is dragged far enough on
				// opposite side, radius gets closer to zero.
				float pageWidth = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT)
						.width();
				double curlLen = radius * Math.PI;
				if (dist > (pageWidth * 2) - curlLen) {
					curlLen = Math.max((pageWidth * 2) - dist, 0f);
					radius = curlLen / Math.PI;
				}

				// Actual curl position calculation.
				if (dist >= curlLen) {
					double translate = (dist - curlLen) / 2;
					if (mViewMode == SHOW_TWO_PAGES) {
						mCurlPos.x -= mCurlDir.x * translate / dist;
					} else {
						float pageLeftX = mRenderer
								.getPageRect(CurlRenderer.PAGE_RIGHT).left;
						radius = Math.max(Math.min(mCurlPos.x - pageLeftX, radius),
								0f);
					}
					mCurlPos.y -= mCurlDir.y * translate / dist;
				} else {
					double angle = Math.PI * Math.sqrt(dist / curlLen);
					double translate = radius * Math.sin(angle);
					mCurlPos.x += mCurlDir.x * translate / dist;
					mCurlPos.y += mCurlDir.y * translate / dist;
				}
			}
			if (mBindPosition == BIND_TOP || mBindPosition == BIND_BOTTOM) {
				// Adjust curl radius so that if page is dragged far enough on
				// opposite side, radius gets closer to zero.
				float pageHeight = -mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT)
						.height();
				double curlLen = radius * Math.PI;
				if (dist > (pageHeight * 2) - curlLen) {
					curlLen = Math.max((pageHeight * 2) - dist, 0f);
					radius = curlLen / Math.PI;
				}

				// Actual curl position calculation.
				if (dist >= curlLen) {
					double translate = (dist - curlLen) / 2;
					if (mViewMode == SHOW_TWO_PAGES) {
						mCurlPos.x -= mCurlDir.x * translate / dist;
					} else {
						float pageLeftX = mRenderer
								.getPageRect(CurlRenderer.PAGE_RIGHT).left;
						radius = Math.max(Math.min(mCurlPos.x - pageLeftX, radius),
								0f);
					}
					mCurlPos.y -= mCurlDir.y * translate / dist;
				} else {
					double angle = Math.PI * Math.sqrt(dist / curlLen);
					double translate = radius * Math.sin(angle);
					mCurlPos.x += mCurlDir.x * translate / dist;
					mCurlPos.y += mCurlDir.y * translate / dist;
				}
			}
		}
		// Otherwise we'll let curl follow pointer position.
		else if (mCurlState == CURL_LEFT) {

			if (mBindPosition == BIND_LEFT || mBindPosition == BIND_RIGHT) {
				// Adjust radius regarding how close to page edge we are.
				float pageLeftX = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).left;
				radius = Math.max(Math.min(mCurlPos.x - pageLeftX, radius), 0f);
	
				float pageRightX = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).right;
				mCurlPos.x -= Math.min(pageRightX - mCurlPos.x, radius);
				mCurlDir.x = mCurlPos.x + mDragStartPos.x;
				mCurlDir.y = mCurlPos.y - mDragStartPos.y;
			}

			if (mBindPosition == BIND_TOP || mBindPosition == BIND_BOTTOM) {
				// Adjust radius regarding how close to page edge we are.
				float pageTopY = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).top;
				radius = Math.max(Math.min(pageTopY - mCurlPos.y , radius), 0f);
	
				float pageBottomY = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).bottom;
				mCurlPos.y -= Math.min(mCurlPos.y - pageBottomY, radius);
				mCurlDir.y = mCurlPos.y + mDragStartPos.y;
				mCurlDir.x = mCurlPos.x - mDragStartPos.x;
			}
			
		}

		setCurlPos(mCurlPos, mCurlDir, radius);
	}

	/**
	 * Updates given CurlPage via PageProvider for page located at index.
	 */
	private void updatePage(CurlPage page, int index) {
		// First reset page to initial state.
		page.reset();
		// Ask page provider to fill it up with bitmaps and colors.
		mPageProvider.updatePage(page, mPageBitmapWidth, mPageBitmapHeight,
				index);
	}

	/**
	 * Updates bitmaps for page meshes.
	 */
	private void updatePages() {
		if (mPageProvider == null || mPageBitmapWidth <= 0
				|| mPageBitmapHeight <= 0) {
			return;
		}

		// Remove meshes from renderer.
		mRenderer.removeCurlMesh(mPageLeft);
		mRenderer.removeCurlMesh(mPageRight);
		mRenderer.removeCurlMesh(mPageCurl);

		int leftIdx = mCurrentIndex - 1;
		int rightIdx = mCurrentIndex;
		int curlIdx = -1;
		if (mCurlState == CURL_LEFT) {
			curlIdx = leftIdx;
			--leftIdx;
		} else if (mCurlState == CURL_RIGHT) {
			curlIdx = rightIdx;
			++rightIdx;
		}

		if (rightIdx >= 0 && rightIdx < mPageProvider.getPageCount()) {
			updatePage(mPageRight.getTexturePage(), rightIdx);
			mPageRight.setFlipTexture(false);
			mPageRight.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
			mPageRight.reset();
			mRenderer.addCurlMesh(mPageRight);
		}
		if (leftIdx >= 0 && leftIdx < mPageProvider.getPageCount()) {
			updatePage(mPageLeft.getTexturePage(), leftIdx);
			mPageLeft.setFlipTexture(true);
			mPageLeft.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
			mPageLeft.reset();
			if (mRenderLeftPage) {
				mRenderer.addCurlMesh(mPageLeft);
			}
		}
		if (curlIdx >= 0 && curlIdx < mPageProvider.getPageCount()) {
			updatePage(mPageCurl.getTexturePage(), curlIdx);

			if (mCurlState == CURL_RIGHT) {
				mPageCurl.setFlipTexture(true);
				mPageCurl.setRect(mRenderer
						.getPageRect(CurlRenderer.PAGE_RIGHT));
			} else {
				mPageCurl.setFlipTexture(false);
				mPageCurl
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
			}

			mPageCurl.reset();
			mRenderer.addCurlMesh(mPageCurl);
		}
	}

	/**
	 * Provider for feeding 'book' with bitmaps which are used for rendering
	 * pages.
	 */
	public interface PageProvider {

		/**
		 * Return number of pages available.
		 */
		public int getPageCount();

		/**
		 * Called once new bitmaps/textures are needed. Width and height are in
		 * pixels telling the size it will be drawn on screen and following them
		 * ensures that aspect ratio remains. But it's possible to return bitmap
		 * of any size though. You should use provided CurlPage for storing page
		 * information for requested page number.<br/>
		 * <br/>
		 * Index is a number between 0 and getBitmapCount() - 1.
		 */
		public void updatePage(CurlPage page, int width, int height, int index);

		/**
		 * Called when the curl index page is updated
		 * 
		 * @param index
		 *			  Udpated index
		 */
		public void indexChanged(int index);
	}

	/**
	 * Simple holder for pointer position.
	 */
	private class PointerPosition {
		PointF mPos = new PointF();
		float mPressure;
	}

	/**
	 * Observer interface for handling CurlView size changes.
	 */
	public interface SizeChangedObserver {

		/**
		 * Called once CurlView size changes.
		 */
		public void onSizeChanged(int width, int height);
	}

}
