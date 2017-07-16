package cmsc435.light_gappedtransfertool;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class CameraOverlay extends View {

    Paint paint;
    private int cameraWidth, cameraHeight;

    public CameraOverlay(Context context) {
        super(context);
        init(null, 0);
    }

    public CameraOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CameraOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        paint = new Paint();
        paint.setAlpha(125);
    }

    public void setCameraDimensions(int width, int height)
    {
        cameraWidth = width;
        cameraHeight = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth(), height = canvas.getHeight();
        int cropWidth = Math.min(540, cameraHeight) * width / cameraHeight;
        int cropHeight = Math.min(960, cameraWidth) * height / cameraWidth;
        canvas.drawRect(0, 0, width/2-cropWidth/2, height, paint);
        canvas.drawRect(width/2+cropWidth/2, 0, width, height, paint);
        canvas.drawRect(width/2-cropWidth/2, 0, width/2+cropWidth/2, height/2-cropHeight/2, paint);
        canvas.drawRect(width/2-cropWidth/2, height/2+cropHeight/2, width/2+cropWidth/2, height, paint);
    }
}
