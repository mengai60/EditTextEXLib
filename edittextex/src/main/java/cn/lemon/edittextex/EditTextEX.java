package cn.lemon.edittextex;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 支持
 * 1.是否需要一键清除输入
 * 2.是否需要密码显示隐藏
 * 3.是否需要显示手机区号前缀
 * 4.自定义前缀
 */
public class EditTextEX extends RelativeLayout implements TextWatcher, View.OnClickListener {

    private EditText editText;
    private int edtId = 0;
    private boolean needClear;
    private boolean needShowHidePassword;
    private boolean needPhoneNumberPrefix;
    private String phoneNumberPrefix;
    private int suffixItemPadding;
    private int suffixRightPadding;

    private boolean hasPrefix;
    private boolean hasSuffix;
    private LinearLayout prefix;
    private LinearLayout suffix;

    private ImageView btn_clear, btn_show_hide;
    private boolean showPassword;

    public EditTextEX(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditTextEX(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int padding = getResources().getDimensionPixelSize(R.dimen.d_22);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditTextEX);
            edtId = typedArray.getResourceId(R.styleable.EditTextEX_edtId, 0);
            needClear = typedArray.getBoolean(R.styleable.EditTextEX_needClear, false);
            needShowHidePassword = typedArray.getBoolean(R.styleable.EditTextEX_needShowHidePassword, false);
            showPassword = typedArray.getBoolean(R.styleable.EditTextEX_ShowPassword, false);
            needPhoneNumberPrefix = typedArray.getBoolean(R.styleable.EditTextEX_needPhoneNumberPrefix, false);
            phoneNumberPrefix = typedArray.getString(R.styleable.EditTextEX_phoneNumberPrefix);
            suffixItemPadding = typedArray.getDimensionPixelSize(R.styleable.EditTextEX_suffixItemPadding, padding);
            suffixRightPadding = typedArray.getDimensionPixelSize(R.styleable.EditTextEX_suffixRightPadding, 0);
            if (TextUtils.isEmpty(phoneNumberPrefix)) {
                phoneNumberPrefix = "+86";
            }
            typedArray.recycle();
        }
        hasSuffix = needClear || needShowHidePassword;
        hasPrefix = needPhoneNumberPrefix;
        if (hasSuffix) {
            suffix = new LinearLayout(context);
            suffix.setOrientation(LinearLayout.HORIZONTAL);
            suffix.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
            suffix.setPadding(0, 0,suffixRightPadding, 0);
        }
        if (needClear) {
            btn_clear = new ImageView(context);
            btn_clear.setImageResource(R.drawable.edt_clear_btn);
            btn_clear.setScaleType(ImageView.ScaleType.CENTER);
            btn_clear.setPadding(suffixItemPadding, 0, 0, 0);
            suffix.addView(btn_clear, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        if (needShowHidePassword) {
            btn_show_hide = new ImageView(context);
            btn_show_hide.setImageResource(R.drawable.edt_show_hide_btn);
            btn_show_hide.setScaleType(ImageView.ScaleType.CENTER);
            btn_show_hide.setPadding(suffixItemPadding, 0, 0, 0);
            suffix.addView(btn_show_hide, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        if (hasPrefix) {
            prefix = new LinearLayout(context);
            prefix.setOrientation(LinearLayout.HORIZONTAL);
            prefix.setGravity(CENTER_VERTICAL);
        }
        if (needPhoneNumberPrefix) {
            TextView tv1 = new TextView(context);
            tv1.setText(phoneNumberPrefix);
            tv1.setTextColor(getResources().getColor(R.color.c_333));
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.d_32));
            tv1.setGravity(Gravity.CENTER);
            prefix.addView(tv1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            View divider = new View(context);
            divider.setBackgroundColor(getResources().getColor(R.color.divider));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);

            int d_8 = getResources().getDimensionPixelSize(R.dimen.d_8);
            params.setMargins(padding, d_8, padding, d_8);
            prefix.addView(divider, params);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        editText = findViewById(edtId);
        if (editText != null) {
            if (hasSuffix) {
                // 设置与EditText等高,宽度取决于EditText的右间距
                LayoutParams suffixParams = new LayoutParams(editText.getPaddingRight(), ViewGroup.LayoutParams.WRAP_CONTENT);
                suffixParams.addRule(ALIGN_TOP, edtId);
                suffixParams.addRule(ALIGN_BOTTOM, edtId);
                suffixParams.addRule(ALIGN_RIGHT, edtId);
                suffixParams.addRule(LEFT_OF, edtId);
                addView(suffix, suffixParams);
                afterTextChanged(editText.getText());
                setPasswordStatus();
                setListener();
            }
            if (hasPrefix) {
                // 设置与EditText等高,宽度取决于EditText的左间距
                prefix.setPadding(0, editText.getPaddingTop(), 0, editText.getPaddingBottom());
                LayoutParams prefixParams = new LayoutParams(editText.getPaddingLeft(), ViewGroup.LayoutParams.WRAP_CONTENT);
                prefixParams.addRule(ALIGN_TOP, edtId);
                prefixParams.addRule(ALIGN_BOTTOM, edtId);
                prefixParams.addRule(ALIGN_LEFT, edtId);
                addView(prefix, prefixParams);
            }
        }
    }

    private void setListener() {
        if (needClear) {
            editText.addTextChangedListener(this);
            btn_clear.setOnClickListener(this);
        }
        if (needShowHidePassword) {
            btn_show_hide.setOnClickListener(this);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (needClear) {
            String text = s.toString();
            btn_clear.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btn_clear) {
            editText.setText("");
        } else if (v == btn_show_hide) {
            changePasswordStatus();
        }
    }

    private void changePasswordStatus() {
        showPassword = !showPassword;
        setPasswordStatus();
    }

    private void setPasswordStatus() {
        if (needShowHidePassword) {
            if (showPassword) {
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            editText.setSelection(editText.getText().length());
            btn_show_hide.setSelected(showPassword);
        }
    }



}
