package com.bupt.iheath.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.iheath.MyApplication;
import com.bupt.iheath.R;
import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.model.UserState;
import com.bupt.iheath.ui.base.BaseActivity;
import com.bupt.iheath.utils.NotifyUtils;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class Analyze2Activity extends BaseActivity {

    private static List<UserState> allData = new ArrayList<>();
    private static List<UserState> weekData ;
    private static List<UserState> dayData ;
    private static List<UserState> hourData ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_column_dependency);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
        setupActionBar();

        getAllUserState(allData, 60);
//        allData = (List<UserState>) MyApplication.sCache.getAsObject("all_data");
//        if (allData == null) {
//            getAllUserState(allData, 60);
//        }

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 显示返回箭头
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllUserState(final List<UserState> data,int limit) {
        AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
        BmobQuery<UserState> query = new BmobQuery<>();
        query.addWhereEqualTo("email", accountInfo.getEmail());
        limit = limit < 100 ? limit : 100 ;
        query.setLimit(limit);
        query.order("-createdAt");
        query.findObjects(this, new FindListener<UserState>() {
            @Override
            public void onSuccess(List<UserState> object) {
                data.clear();
                for (UserState userState : object) {
                    data.add(userState);
                }
                weekData = data ;
                dayData = data.subList(15, 30) ;
                hourData = data.subList(0, 20);
            }

            @Override
            public void onError(int code, String msg) {
            }
        });
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public final  String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec",};

        public final  String[] months2 = new String[]{"最近一小时", "最近一天", "最近一周"};

        public final  String[] days = new String[]{"Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun",};

        private LineChartView chartTop;
        private ColumnChartView chartBottom;

        private LineChartData lineData;
        private ColumnChartData columnData;

        private TextView tv ;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_line_column_dependency, container, false);

            // *** TOP LINE CHART ***
            chartTop = (LineChartView) rootView.findViewById(R.id.chart_top);

            tv = (TextView) rootView.findViewById(R.id.cal_txt) ;

            // Generate and set data for line chart
            generateInitialLineData();

            // *** BOTTOM COLUMN CHART ***

            chartBottom = (ColumnChartView) rootView.findViewById(R.id.chart_bottom);

            generateColumnData();

            return rootView;
        }

        private void generateColumnData() {

            int numSubcolumns = 1;
            int numColumns = months2.length;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            for (int i = 0; i < numColumns; ++i) {

                values = new ArrayList<SubcolumnValue>();
                int color ;
                if (i == 0) {
                    color = ChartUtils.COLOR_VIOLET ;
                } else if (i == 1) {
                    color = ChartUtils.COLOR_BLUE ;
                } else {
                    color = ChartUtils.COLOR_ORANGE ;
                }
                values.add(new SubcolumnValue(1f, color));
//                for (int j = 0; j < numSubcolumns; ++j) {
//                    //values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
//                    values.add(new SubcolumnValue(1f, ChartUtils.pickColor()));
//                }

                axisValues.add(new AxisValue(i).setLabel(months2[i]));

                columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
            }

            columnData = new ColumnChartData(columns);

            columnData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            //columnData.setAxisYLeft(new Axis().setHasLines(false).setMaxLabelChars(2));
            chartBottom.setColumnChartData(columnData);

            // Set value touch listener that will trigger changes for chartTop.
            chartBottom.setOnValueTouchListener(new ValueTouchListener());

            // Set selection mode to keep selected month column highlighted.
            chartBottom.setValueSelectionEnabled(true);

            chartBottom.setZoomType(ZoomType.HORIZONTAL);

        }

        /**
         * Generates initial data for line chart. At the begining all Y values are equals 0. That will change when user
         * will select value on column chart.
         */
        private void generateInitialLineData() {
            int numValues = 7;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<PointValue> values = new ArrayList<PointValue>();
            for (int i = 0; i < numValues; ++i) {
                values.add(new PointValue(i, 0));
                axisValues.add(new AxisValue(i).setLabel(String.valueOf(i)));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GREEN).setCubic(true);

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);

            lineData = new LineChartData(lines);
            lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

            chartTop.setLineChartData(lineData);

            // For build-up animation you have to disable viewport recalculation.
            chartTop.setViewportCalculationEnabled(false);

            // And set initial max viewport and current viewport- remember to set viewports after data.
            Viewport v = new Viewport(0, 250, 6, 0);
            chartTop.setMaximumViewport(v);
            chartTop.setCurrentViewport(v);

            chartTop.setZoomType(ZoomType.HORIZONTAL);
        }

        private void generateLineData(int color, float range, int index) {
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation();

            int step1 = hourData.get(0).getStep() ;
            int step2 = dayData.get(0).getStep() ;
            String hourStr = "已走: "+ step1/6 +" 步"+"\n"+"消耗: "+calc_calorie(step1/6)+" 卡路里" ;
            String dayStr = "已走: "+ (step1+step2) + " 步"+"\n"+"消耗: "+ (int)(calc_calorie(step1+step2)*18.23f) + " 卡路里" ;
            String weekStr = "已走: "+ (step1+step2)*6+" 步"+"\n"+"消耗: "+(int)(calc_calorie((step1+step2)*5)*18.23*3.265f)+" 卡路里" ;
            switch (index) {
                case 0:
                    //NotifyUtils.showHints(hourStr);
                    tv.setText(hourStr);
                    setUpData(hourData, color);
                    break;
                case 1:
                    //NotifyUtils.showHints(dayStr);
                    tv.setText(dayStr);
                    setUpData(dayData, color);
                    break;
                case 2:
                    //NotifyUtils.showHints(weekStr);
                    tv.setText(weekStr);
                    setUpData(weekData, color);
                    break;
                default:
                    generateInitialLineData();
                    break;
            }

            // Start new data animation with 300ms duration;
            chartTop.startDataAnimation(300);
        }

        private void setUpData(List<UserState> data, int color) {
            int numValues = data.size();

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<PointValue> values = new ArrayList<PointValue>();
            for (int i = 0; i < numValues; ++i) {
                values.add(new PointValue(i, data.get(i).getHeartRate()));
                axisValues.add(new AxisValue(i).setLabel(String.valueOf(i)));
            }

            Line line = new Line(values);
            line.setColor(color).setCubic(true);

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);

            lineData = new LineChartData(lines);
            lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

            chartTop.setLineChartData(lineData);

            // For build-up animation you have to disable viewport recalculation.
            chartTop.setViewportCalculationEnabled(false);

            Viewport v = new Viewport(0, 250, numValues, 0);
            chartTop.setMaximumViewport(v);
            chartTop.setCurrentViewport(v);

            chartTop.setZoomType(ZoomType.HORIZONTAL);

        }

        private float calc_calorie(int step) {
            // cal = mile * weight * 1.036 ;
            AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser();
            float weight ;
            try {
                weight = accountInfo.getWeight() ;
            }catch (Exception e) {
                e.printStackTrace();
                weight = 63f ;
            }
            return (float) (weight*1.036* step * 0.7) ;
        }

        private class ValueTouchListener implements ColumnChartOnValueSelectListener {

            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {

                generateLineData(value.getColor(), 250, columnIndex);
            }

            @Override
            public void onValueDeselected() {

                generateLineData(ChartUtils.COLOR_GREEN, 0, -1);

            }
        }
    }
}
