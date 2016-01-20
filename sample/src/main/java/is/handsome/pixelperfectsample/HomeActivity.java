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
import is.handsome.pixelperfect.MockupImage;
import is.handsome.pixelperfect.PixelPerfect;
import is.handsome.pixelperfectsample.util.VisualWarnTree;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

    private Button pixelPerfectButton;
    private View pixelPerfectTextView;

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
        pixelPerfectTextView = findViewById(R.id.pixel_perfect_text_view);

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

    private List<MockupImage> createImages() {
        List<MockupImage> images = new ArrayList<>(2);

        pixelPerfectButton.setVisibility(View.INVISIBLE);

        MockupImage correctImage = new MockupImage();
        correctImage.name = "Correct";
        correctImage.bitmap = takeActivityScreenshot();
        images.add(correctImage);

        pixelPerfectButton.setVisibility(View.VISIBLE);

        return images;
    }

    private Bitmap takeActivityScreenshot() {
        View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }
}
