package com.cortxt.app.MMC.ActivitiesOld.CustomViews;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.cortxt.app.MMC.R;
import com.cortxt.app.MMC.Activities.CompareNew;
import com.cortxt.app.MMC.ActivitiesOld.CustomViews.Chart.Chart;
import com.cortxt.app.MMC.Reporters.ReportManager;
import com.cortxt.app.MMC.ServicesOld.MMCPhoneStateListenerOld;
import com.cortxt.app.MMC.Utils.FontsUtil;
import com.cortxt.app.MMC.Utils.MmcConstants;
import com.cortxt.app.MMC.UtilsOld.Carrier;

public class SpeedStatChartNew extends StatChartNew {

	/*
	 * ==========================================================
	 * Start private variables
	 */

	/**
	 * This is the distance between the x axis and the time labels
	 */
	private static final int XAXIS_LABEL_YOFFSET = 14;
	/**
	 * This is the distance by which all the labels on the x axis are moved left.
	 */
	private static final int XAXIS_LABEL_XOFFSET = 0;
	/**
	 * The last label in the x axis is the "now" keyword. This is a larger word than the rest and thus requires an
	 * additional offset to fit into the screen
	 */
	private static final float XAXIS_STROKE_WIDTH = 1.67f;// 2.67f;
	/**
	 * Stroke width of the time series on the chart.
	 */
	private static final float CHART_SERIES_STROKE_WIDTH = 1.3f;

	private static final int BAR_SPACING = 10;

	public static final float CHART_XAXIS_FONT_SIZE = 13.0f;
	/**
	 * Tag for debugging
	 */
	private static final String TAG = Chart.class.getSimpleName();

	// This is a general time variable that will be constantly "setToNow"
	Time time = new Time(Time.getCurrentTimezone());
	int[] flatcolors = { Color.parseColor("#0099CC"), Color.parseColor("#0099CC"), Color.parseColor("#FBB03B"), Color.parseColor("#FBB03B"), Color.parseColor("#FF0000"), Color.parseColor("#FF0000") };
	// Bright end of gradients for the 3 bars
	int[] colors = { Color.rgb(0x29, 0xAB, 0xE2), Color.rgb(0xFF, 0xD5, 0x61), Color.rgb(0xFF, 0x00, 0x00) };
	// Dark end of gradients for the 3 bars
	int[] colors2 = { Color.rgb(0x29, 0x66, 0xCC), Color.rgb(0xF7, 0x87, 0x00), Color.rgb(0x99, 0x20, 0x27) };
	
	final int TOTAL_BAR_CHARTS = 6;
	Integer[] iSeries = new Integer[TOTAL_BAR_CHARTS];
	float yMax = 100, yInc = 25;

	// new colors
	private int[] newBarColors = { 0xff00a99d, 0xffed1e79, 0xff9e005d };
	private int[] newTextColors = { 0xff5d5d5d, 0xff666666 };// [0] for title Call Stats Benchmark, [1] for labels
	private int arrowsColor = 0xffffffff;

	private float[] newTextSize = { 24f * screenDensityScale, 18f * screenDensityScale, 16f * screenDensityScale };
	private String PAGE_TITLE = null;//context.getString((int) Compare.STRINGS.get("dataspeeds"));
	private String[] labels = { context.getString((int) CompareNew.STRINGS.get("yourphone")), context.getString((int) CompareNew.STRINGS.get("yourcarrier")), context.getString((int) CompareNew.STRINGS.get("allcarriers")) };

	private Rect textScale = new Rect();
	private boolean contentScaled = false;

	// Constants
	private Typeface robotoRegular = FontsUtil.getCustomFont(MmcConstants.font_Regular, context);
	private Typeface robotoLight = FontsUtil.getCustomFont(MmcConstants.font_Light, context);
	private Typeface robotoMedium = FontsUtil.getCustomFont(MmcConstants.font_MEDIUM, context);

	private float separator = 6.0f * screenDensityScale;
	private float barWidth = 18.0f * screenDensityScale;
	private Rect speedTextRect = new Rect();
	private float maxSpeed = 0f;
	
	/*
	 * End private variables
	 * ==========================================================
	 * Start constructors
	 */
	public SpeedStatChartNew(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public SpeedStatChartNew(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	// initialize the values for the chart drawing
	private void init() {
		DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
		screenDensityScale = dm.density;
		if(isTallScreen(context)){
			CHART_TO_SCREEN_RATIO = 0.8f;
		}else{
			CHART_TO_SCREEN_RATIO = 0.84f;			
		}
		paint = new Paint();
		paint.setAntiAlias(true);
		CHART_LEFT_PADDING = 40;
		CHART_RIGHT_PADDING = 40;
		PAGE_TITLE = getTitle();
		if (TITLE_REFERENCE == null) {
			TITLE_REFERENCE = PAGE_TITLE;
		}
		int customTitles = getResources().getInteger(R.integer.CUSTOM_COMPARENAMES);
		if (customTitles == 1)
		{
			labels[0] = context.getString((int) CompareNew.CUST_STRINGS.get("yourphone"));
			labels[1] = context.getString((int) CompareNew.CUST_STRINGS.get("yourcarrier"));
			labels[2] = context.getString((int) CompareNew.CUST_STRINGS.get("allcarriers"));
		}
	}

	/**
	 * get title for this section
	 * @return
	 */
	public String getTitle() {
		CompareNew compare = (CompareNew)context;
		return compare.getString("downloadspeed");
//		int networkType = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkType();
//		int speedTier = MMCPhoneStateListenerOld.getNetworkGeneration(networkType);
//		int customTitles = getResources().getInteger(R.integer.CUSTOM_COMPARENAMES);
//
//		if (customTitles == 1) {
//			int speedtitle = R.string.mystats_custom_downloadspeed;
//			switch (speedTier) {
//			case 0:
//				speedtitle = R.string.mystats_custom_downloadspeed;
//				break;
//			case 1:
//			case 2:
//				speedtitle = R.string.mystats_custom_downloadspeed2G;
//				break;
//			case 3:
//			case 4:
//				speedtitle = R.string.mystats_custom_downloadspeed3G;
//				break;
//			case 5:
//				speedtitle = R.string.mystats_custom_downloadspeedLTE;
//				break;
//			case 10:
//				speedtitle = R.string.mystats_custom_downloadspeedWifi;
//				break;
//			}
//			return context.getString(speedtitle);
//		} else {
//			int speedtitle = R.string.mystats_downloadspeed;
//			switch (speedTier) {
//			case 0:
//				speedtitle = R.string.mystats_downloadspeed;
//				break;
//			case 1:
//			case 2:
//				speedtitle = R.string.mystats_downloadspeed2G;
//				break;
//			case 3:
//			case 4:
//				speedtitle = R.string.mystats_downloadspeed3G;
//				break;
//			case 5:
//				speedtitle = R.string.mystats_downloadspeedLTE;
//				break;
//			case 10:
//				speedtitle = R.string.mystats_downloadspeedWifi;
//				break;
//			}
//			return context.getString(speedtitle);
//		}
	}
	/*
	 * Compare Screen parent sends the json statistics to the chart
	 */
	@Override
	public void setStats(JSONObject stats) {
		super.setStats(stats);
		mStats = stats;
		if (stats == null)
			return;
		Carrier currentCarrier = ReportManager.getInstance(context.getApplicationContext()).getCurrentCarrier();
		String currOpid = "0";
		if (currentCarrier != null)
			currOpid = currentCarrier.OperatorId;
		String[] keys = { "yourphone", currOpid, "0" };
		int i;
		float max = 0;
		for (i = 0; i < 3; i++) {
			try {
				if (mStats != null && mStats.has(keys[i])) {
					JSONObject stat = mStats.getJSONObject(keys[i]);
					iSeries[2 * i] = Integer.parseInt(stat.getString(ReportManager.StatsKeys.DOWNLOAD_SPEED_AVERAGE)) / 1000;
					iSeries[2 * i + 1] = Integer.parseInt(stat.getString(ReportManager.StatsKeys.UPLOAD_SPEED_AVERAGE)) / 1000;
					max = Math.max(max, iSeries[2 * i]);
					max = Math.max(max, iSeries[2 * i + 1]);
				} else {
					iSeries[2 * i] = 0;
					iSeries[2 * i + 1] = 0;
				}
			} catch (Exception e) {
				iSeries[2 * i] = 0;
				iSeries[2 * i + 1] = 0;
			}
			
			//TODO remove next 3
//			iSeries[2*i] = 700;
//			iSeries[2*i+1] = 1100;
//			max = 1100;
			
		}

		maxSpeed = max;
		yMax = max * 5 / 4;
		yInc = yMax / 5;
		shaders = null;
	}

	/*
	 * Set heights relative to screen height for separators and bars
	 * fonts will take screen width into account as well
	 */
	private void setHeightRatio() {
		float titleHeight = (chartHeight/10f);
		// colSpace space available on screen for width of the horizontal column -- 3 columns in total
		float colSpace = (chartHeight - titleHeight - separator)/3f;
		// width of unscaled column
		float colScale = newTextSize[1] + separator * 4 + barWidth * 2;

		float hRatio = (colSpace / colScale) * screenDensityScale;
		separator = 6f * hRatio; // top margin for labels e.g. 'Your Phone'
		barWidth = 18f * hRatio; // width of the horizontal bar

		float scaledWidth = calcTextWidth(PAGE_TITLE, robotoRegular, 24f);// scale ratio for chart title
		newTextSize[0] = calcFontSize(PAGE_TITLE, scaledWidth, titleHeight, robotoRegular, 24f * hRatio);
		newTextSize[1] = calcFontSize(labels[1], chartWidth/2, 18f*hRatio, robotoLight, 18f * hRatio);
		newTextSize[2] = 16f * hRatio; // let it scale by hRatio since its against bar which is also scaled by same factor

		contentScaled = true;
	}

	/*
	 * End Constructors
	 * ===========================================================
	 * Start over-ridden methods (from view)
	 */

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(iSeries[0] == null){
			// stats not populated yet
			return;
		}
		if (!contentScaled) {
			setHeightRatio();
		}
		float top = (CHART_TOP_PADDING + XAXIS_STROKE_WIDTH) * screenDensityScale;
		float left = CHART_LEFT_PADDING * screenDensityScale;

		//TODO remove next 2 lines
//		paint.setColor(Color.GRAY);
//		canvas.drawRect(left, top, left+chartWidth, top+chartHeight, paint);
		
		// Paint settings for title 'Call Stats Benchmark'
		paint.setColor(newTextColors[0]);
		paint.setTextSize(newTextSize[0]); // 24
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTypeface(robotoRegular);
		textScale.setEmpty();
		
		// use TITLE_REFERENCE height for all titles to place them vertically aligned
		paint.getTextBounds(TITLE_REFERENCE, 0, TITLE_REFERENCE.length(), textScale);

		top += textScale.height();
		canvas.drawText(PAGE_TITLE, left + chartWidth / 2, top, paint);
		paint.setTextAlign(Paint.Align.LEFT);

		top += separator; // add gap b/w title and rest of chart elements

		top = drawColumn(canvas, labels[0], iSeries[0], iSeries[1], newBarColors[0], left, top); // yourphone
		top = drawColumn(canvas, labels[1], iSeries[2], iSeries[3], newBarColors[1], left, top); // yourcarrier
		top = drawColumn(canvas, labels[2], iSeries[4], iSeries[5], newBarColors[2], left, top); // allcarriers
	}

	/*
	 * End over-ridden methods
	 * ===========================================================
	 * Start helper methods
	 */
	
	/**
	 * Draw a bar column
	 * @param canvas
	 * @param label
	 * @param dSpeed Download speed
	 * @param uSpeed upload speed
	 * @param barColor color of the bar
	 * @param left left coordinate
	 * @param top top coordinate
	 * @return bottom coordinate of vertically lowest element drawn
	 */
	private float drawColumn(Canvas canvas, String label, int dSpeed, int uSpeed, int barColor, float left, float top) {

		float animator = animatePercentage() / 100f;
		paint.setColor(newTextColors[1]);
		paint.setTextSize(newTextSize[1]); // 18
		paint.setTypeface(robotoLight);
		textScale.setEmpty();
		paint.getTextBounds(labels[0], 0, 2, textScale);
		
		top += textScale.height() + separator; // update y-coordinate for text label
		canvas.drawText(label, left, top, paint);

		top += separator; // update y-coordinate for first bar in the column
		getTextBounds(maxSpeed + "", robotoMedium, newTextSize[2], speedTextRect); // 'speed' text bounds
		// vertically center align text wit bar
		float textTopOffset = speedTextRect.height() + (barWidth - speedTextRect.height()) / 2; 
		paint.setTypeface(robotoMedium);
		paint.setTextSize(newTextSize[2]); // 16
		double downMbps = (dSpeed/10)/1.00;
		canvas.drawText(downMbps + "", left, top + textTopOffset, paint);

		paint.setColor(barColor);
		float barXOffset = speedTextRect.width() + separator;
		float dstUnit = (chartWidth - barXOffset) / (float) maxSpeed * animator;
		float right1 = left + barXOffset + (dSpeed * dstUnit);
		canvas.drawRect(left + barXOffset, top, right1, top + barWidth, paint);
		drawTriangle(canvas, left + barXOffset, top, barWidth / 2, false);

		// update y-coordinate for second bar in the column
		top += barWidth + separator;
		// draw speed for second bar
		paint.setColor(newTextColors[1]);
		double upMbps = (uSpeed/10)/1.00;
		canvas.drawText(upMbps + "", left, top + textTopOffset, paint);
		// draw second bar in this column
		paint.setColor(barColor);
		float right2 = left + barXOffset + (uSpeed * dstUnit);
		canvas.drawRect(left + barXOffset, top, right2, top + barWidth, paint);
		// draw indicator triangle for second bar in this column
		drawTriangle(canvas, left + barXOffset, top, barWidth / 2, true);
		
		// return bottom of vertically lowest element drawn (including separator after the bar)
		return top + barWidth + separator;
	}

	/**
	 * Draws a white triangle at give <code>left</code> and <code>top</code>. Every side is equal to given <code>size</code>
	 * @param canvas
	 * @param left
	 * @param top
	 * @param size
	 * @param down
	 */
	private void drawTriangle(Canvas canvas, float left, float top, float size, boolean down) {
		top += size/2f; // top offset, so that triangle is vertically centered in bar
		left += size/2f; // left offset
 		// x1, y1, x2, y2, x3, y3
		float verts[] = { left, top, left + size, top, left + size / 2, top + size };
		if (down) {
			verts[0] = left + (size / 2);
			verts[1] = top;
			verts[2] = left;
			verts[3] = top + size;
			verts[4] = left + size;
			verts[5] = top + size;
		}
		/*	
 		int verticesColors[] = { Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE };
		canvas.drawVertices(Canvas.VertexMode.TRIANGLES, verts.length, verts, 0, null, 0, verticesColors, 0, null, 0, 0, new Paint());\
		*/
		// test white square
		// canvas.drawRect(left, top, left+size, top+size, paint);
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		paint.setStrokeWidth(1);
		paint.setColor(arrowsColor);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);
		
		Path path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);
		path.moveTo(verts[0], verts[1]);
		path.lineTo(verts[2], verts[3]);
		path.lineTo(verts[4], verts[5]);
		path.close();

		canvas.drawPath(path, paint);
	}

	/*
	 * ========================================================
	 */
}
