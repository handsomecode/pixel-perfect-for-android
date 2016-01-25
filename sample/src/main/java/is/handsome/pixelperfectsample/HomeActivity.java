package is.handsome.pixelperfectsample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import is.handsome.pixelperfect.PixelPerfect;
import is.handsome.pixelperfect.PixelPerfectImage;
import is.handsome.pixelperfectsample.util.VisualWarnTree;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

    private Button pixelPerfectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();

        if (savedInstanceState == null) {
            if (BuildConfig.DEBUG) {
                Timber.plant(new Timber.DebugTree());
                Timber.plant(new VisualWarnTree(this));
            } else {
                Fabric.with(this, new Crashlytics());
            }
        }
    }

    private void init() {
        pixelPerfectButton = (Button) findViewById(R.id.pixel_perfect_button);

        if (PixelPerfect.isShown()) {
            pixelPerfectButton.setText(R.string.button_hide);
        }
        pixelPerfectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PixelPerfect.isCreated()) {
                    PixelPerfect.create().withImages(createImages()).show(HomeActivity.this);
                    pixelPerfectButton.setText(R.string.button_hide);
                } else {
                    if (PixelPerfect.isShown()) {
                        PixelPerfect.hide();
                        pixelPerfectButton.setText(R.string.button_show);
                    } else {
                        PixelPerfect.show();
                        pixelPerfectButton.setText(R.string.button_hide);
                    }
                }
            }
        });
    }

    private List<PixelPerfectImage> createImages() {
        List<PixelPerfectImage> images = new ArrayList<>(1);

        pixelPerfectButton.setVisibility(View.INVISIBLE);

        images.add(createCorrectImage());
        images.add(createIncorrectImage());

        pixelPerfectButton.setVisibility(View.VISIBLE);

        return images;
    }

    private PixelPerfectImage createCorrectImage() {
        PixelPerfectImage correctImage = new PixelPerfectImage();
        correctImage.name = "Correct";
        correctImage.bitmap = takeScreenshot();
        return correctImage;
    }

    private PixelPerfectImage createIncorrectImage() {
        View correctTextView = findViewById(R.id.pixel_perfect_correct_text_view);
        View incorrectTextView = findViewById(R.id.pixel_perfect_incorrect_text_view);

        correctTextView.setVisibility(View.INVISIBLE);
        incorrectTextView.setVisibility(View.VISIBLE);

        PixelPerfectImage incorrectImage = new PixelPerfectImage();
        incorrectImage.name = "Incorrect";
        incorrectImage.bitmap = takeScreenshot();

        correctTextView.setVisibility(View.VISIBLE);
        incorrectTextView.setVisibility(View.GONE);
        return incorrectImage;

    }

    private Bitmap takeScreenshot() {
        View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap screenshotBitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        return screenshotBitmap;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (PixelPerfect.isCreated()) {
            PixelPerfect.destroy();
        }
    }
}
