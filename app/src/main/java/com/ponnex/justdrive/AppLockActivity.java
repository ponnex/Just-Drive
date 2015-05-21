package com.ponnex.justdrive;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by EmmanuelFrancis on 5/21/2015.
 */

public class AppLockActivity extends Activity{
    public static Activity dismiss;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppLockDialog otherClass = new AppLockDialog(this);
        otherClass.Dialog();
        dismiss = this;
    }
}
