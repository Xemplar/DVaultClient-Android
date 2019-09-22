package com.xemplarsoft;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xemplarsoft.libs.crypto.server.domain.Entity;

import java.math.BigDecimal;

public final class Vars {
    public static final String pubkey = "wggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQDuF683gqa+tuNr4VHX09x6rLJetBYhEQjUQOOfEAdreqhhDATB8hSbF04gBSSl2MH+rFlGy3BoGW1CZZ+MwyjykxbMbVHNp+dDCI2NJQE12lR/BSrGQGtti67WcIy3bjjUweNU5hPIvPfWvP4/infsWqtzuqckqQ970ngWTrIeVGz1M62xStB3amIt6XgB+rHSKuGN7PnsxW8n7avXBPKos/thSo3gl0slvi5wbg6SOhVrvw0DrTg/MVKmqipfie9sKH0HJmR8yJB4+AkIIgQ/02w5qh9cTu3z2Uw0BsQ8nZl04GDzpTCwEMJsQDFp+dum2P8u7tn7unYFSQrDAKCJpn2TLL/reZSc7icfU2l3ruT7Bw54+JDMxKQTNUm6DhNo1znAx110H/1U6hR7+HVf0sqU3+htrtcSAU1LmdxCjYtnQ2bvhiFxxVAoW98v0dtUNYdtdtradeelgCLTtWUVcNzbOfbVYb+XqJYdMuejY5+cVNbzsdZWflaHUsnRo6lPzVwyyu8iWO+rk/dgKcBDXG9ANqjVX6gPqdDGHvwoAwxw55YTggW5aO37O3YHXZlCxSZGreMUF/3YkhB2nYzKbUJNLgzl7U8879iwC19vZGj3QY3TrWkDNxlWj35JzYnpzv7+NxtcApp1y3ghRpuf71ofDBlk7g4EvyPqPWiu2QIDAQAB";
    public static final String HOST = "10.0.0.112";
    public static final int PORT = 24516;
    public static final String dbLoc = "databases/dat.db";

    public static final int REQUEST_CODE_QR_SCAN = 101;
    public static final String KEY_ADDRESSES = "addresses";

    public static final BigDecimal TX_FEE = new BigDecimal("0.00001");

    public static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(Context con, int verticalSpaceHeight) {
            this.verticalSpaceHeight = (int)dpToPx(con, verticalSpaceHeight);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight;
            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = verticalSpaceHeight;
            }
        }
    }
    public static float dpToPx(Context con, float dp){
        return dp * ((float) con.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    public static float pxToDp(Context con, float px){
        return px / ((float) con.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String serialize(Object obj, boolean pretty) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            if (pretty) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            }

            return mapper.writeValueAsString(obj);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static Entity deserialize(String str, Class<? extends Entity> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Entity obj = mapper.readValue(str, clazz);

            return obj;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
