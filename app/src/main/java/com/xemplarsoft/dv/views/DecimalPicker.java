package com.xemplarsoft.dv.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xemplarsoft.dv.R;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class DecimalPicker extends RelativeLayout implements OnValueChangeListener {
    private Context context;
    private AttributeSet attrs;
    private int styleAttr;
    private OnClickListener mListener;
    private BigDecimal minValue, maxValue, lastNumber, currentNumber;
    private EditText editText;
    private String format;
    private OnValueChangeListener onValueChangeListener;

    public DecimalPicker(Context context) {
        super(context);
        this.context = context;
    }

    public DecimalPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
    }

    public DecimalPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.styleAttr = defStyleAttr;
    }

    public void init(BigDecimal max, BigDecimal min){
        this.maxValue = max;
        this.minValue = min;
        initView();
    }

    private void initView(){
        inflate(context, R.layout.decimal_picker, this);

        final Resources res = getResources();
        final int defaultColor = res.getColor(R.color.ui_black);
        final int defaultTextColor = res.getColor(R.color.ui_white);
        final Drawable defaultDrawable = res.getDrawable(R.drawable.decimal_picker_shape);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DecimalPicker, styleAttr, 0);

        float textSize = a.getDimension(R.styleable.DecimalPicker_textSize, 24);
        int color = a.getColor(R.styleable.DecimalPicker_backGroundColor,defaultColor);
        int textColor = a.getColor(R.styleable.DecimalPicker_textColor,defaultTextColor);
        Drawable drawable = a.getDrawable(R.styleable.DecimalPicker_backgroundDrawable);

        this.setOnValueChangeListener(this);

        Button buttonMinus = findViewById(R.id.subtract_btn);
        Button buttonPlus = findViewById(R.id.add_btn);
        editText = findViewById(R.id.number_counter);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    BigDecimal num = new BigDecimal(((EditText) v).getText().toString());
                    setNumber(num, true);
                }
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString().trim();
                BigDecimal valueDouble = new BigDecimal("-1");
                try {
                    valueDouble = new BigDecimal(value.isEmpty() ? "0" : value);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (valueDouble.compareTo(new BigDecimal(0)) >= 0){
                    lastNumber = currentNumber;
                    currentNumber = valueDouble;
                    callListener(DecimalPicker.this);
                }
            }
        });

        LinearLayout mLayout = findViewById(R.id.decimal_picker_layout);

        buttonMinus.setTextColor(textColor);
        buttonPlus.setTextColor(textColor);
        editText.setTextColor(textColor);
        buttonMinus.setTextSize(textSize);
        buttonPlus.setTextSize(textSize);
        editText.setTextSize(textSize);

        if (drawable == null){
            drawable = defaultDrawable;
        }
        assert drawable != null;
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
        if (Build.VERSION.SDK_INT > 16)
            mLayout.setBackground(drawable);
        else
            mLayout.setBackgroundDrawable(drawable);

        editText.setText(String.valueOf(minValue));

        currentNumber = minValue;
        lastNumber = minValue;

        buttonMinus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View mView) {
                BigDecimal num = new BigDecimal(editText.getText().toString());
                setNumber(num.subtract(new BigDecimal(1)));
            }
        });
        buttonPlus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View mView) {
                BigDecimal num = new BigDecimal(editText.getText().toString());
                setNumber(num.add(new BigDecimal(1)));
            }
        });
        a.recycle();
    }

    public void onValueChange(DecimalPicker view, BigDecimal oldValue, BigDecimal newValue) {
        BigDecimal n = newValue;
        n = n.setScale(8, BigDecimal.ROUND_DOWN);
        if (n.compareTo(maxValue) > 0) {
            n = maxValue;
            editText.setText(n.toPlainString());
        } else if (n.compareTo(minValue) < 0) {
            n = minValue;
            editText.setText(n.toPlainString());
        }

        lastNumber = currentNumber;
        if(editText.getText().toString().equals("")){
            currentNumber = new BigDecimal(0);
        } else {
            currentNumber = new BigDecimal(editText.getText().toString());
        }
    }

    private void callListener(View view){
        if (mListener != null)
            mListener.onClick(view);

        if (onValueChangeListener != null && lastNumber != currentNumber)
            onValueChangeListener.onValueChange(this, lastNumber, currentNumber);
    }

    public BigDecimal getNumber(){
        return currentNumber;
    }

    public void setNumber(BigDecimal n) {
        System.out.println(n.toPlainString());
        n = n.setScale(8, BigDecimal.ROUND_DOWN);
        try {
            if (n.compareTo(maxValue) > 0) {
                n = maxValue;
                editText.setText(n.toPlainString());
            } else if (n.compareTo(minValue) < 0) {
                n = minValue;
                editText.setText(n.toPlainString());
            } else {
                editText.setText(n.toPlainString());
            }


            lastNumber = currentNumber;
            currentNumber = new BigDecimal(editText.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }


    private String removeTrailingZeroes(String num) {
        NumberFormat nf = NumberFormat.getInstance();
        if (nf instanceof DecimalFormat) {
            DecimalFormatSymbols sym = ((DecimalFormat) nf).getDecimalFormatSymbols();
            char decSeparator = sym.getDecimalSeparator();
            String[] split = num.split((decSeparator == '.' ? "\\" : "") + String.valueOf(decSeparator));
            if (split.length == 2 && split[1].replace("0", "").isEmpty())
                num = split[0];
        }
        return num;
    }

    public void setNumber(BigDecimal n, boolean notifyListener){
        setNumber(n);
        if (notifyListener)
            callListener(this);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mListener = onClickListener;
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener){
        this.onValueChangeListener = onValueChangeListener;
    }

    public interface OnClickListener {
        void onClick(View view);
    }

    public void setRange(BigDecimal startingNumber, BigDecimal endingNumber) {
        minValue = startingNumber;
        maxValue = endingNumber;
    }

    public void setFormat(String format){
        this.format = format;
    }
}