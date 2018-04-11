package mureung.wifiapconnection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Html;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by user on 2018-01-29.
 */

public class DialogManager extends AlertDialog.Builder {

    private AlertDialog.Builder builder;
    public DialogManager(Context context) {
        super(context);
        builder = new AlertDialog.Builder(context);
    }

    /*// 라디오 버튼 다이얼로그
    public void singleChoiceDialog(String title, String[] menuitem, int checkedItem, DialogInterface.OnClickListener singleChoiceItemClickListener, String positiveMessage, DialogInterface.OnClickListener potiveBtnClickListener){
        builder.setTitle(title)
                .setSingleChoiceItems(menuitem, checkedItem, singleChoiceItemClickListener)
                .setPositiveButton(positiveMessage, potiveBtnClickListener)
                .show();
    }

    // 확인창 다이얼로그
    public void positiveDialog(String title, String message, String positiveMessage,DialogInterface.OnClickListener positiveBtnClickListener){
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveMessage,  positiveBtnClickListener)
                .show();
    }*/

    // 확인창 다이얼로그
    public void positiveDialog(String title, String message, String positiveMessage,DialogInterface.OnClickListener positiveBtnClickListener){
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveMessage,  positiveBtnClickListener)
                .show();
    }


    // 확인, 취소 버튼 다이얼로그
    public void positiveNegativeDialog(String title, String message, String positiveMessage , final DialogInterface.OnClickListener positiveBtnClickListener, String negativeMessage, DialogInterface.OnClickListener negativeBtnClickListener){
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveMessage, positiveBtnClickListener)
                .setNegativeButton(negativeMessage, negativeBtnClickListener);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btn_positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button btn_negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                LinearLayout parent = (LinearLayout)btn_positive.getParent();
                parent.removeAllViews();

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                btn_positive.setLayoutParams(layoutParams);
                btn_negative.setLayoutParams(layoutParams);

                btn_positive.setTextSize(20);
                btn_negative.setTextSize(20);

                btn_positive.setTextColor(Color.rgb(0, 85, 166));
                btn_negative.setTextColor(Color.rgb(0, 85, 166));

                btn_positive.setAllCaps(false);
                btn_negative.setAllCaps(false);

                parent.addView(btn_negative);
                parent.addView(btn_positive);
            }
        });
        alertDialog.show();

    }

}
