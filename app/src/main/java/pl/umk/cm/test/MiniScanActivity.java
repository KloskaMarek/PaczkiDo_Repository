package pl.umk.cm.test;

import android.os.Bundle;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class MiniScanActivity extends CaptureActivity {
    @Override
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.activity_mini_scan);
        return (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);
    }
}
