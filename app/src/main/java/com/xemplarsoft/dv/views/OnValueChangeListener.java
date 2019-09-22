package com.xemplarsoft.dv.views;

import java.math.BigDecimal;

public interface OnValueChangeListener {
    void onValueChange(DecimalPicker view, BigDecimal oldValue, BigDecimal newValue);
}
