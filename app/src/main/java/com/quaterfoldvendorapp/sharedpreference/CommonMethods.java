package com.quaterfoldvendorapp.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Environment;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CommonMethods
	{

		public static final String OTP_DELIMITER = ":";
        public static final String SMS_ORIGIN = "BREAKIN";
		public static  String FOLDER_PATH = Environment.getExternalStorageDirectory().toString()+"/MYMFNOW/";

		public static void DisplayToast(Context context, String message)
			{
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();;
			}


		public static int getAge(int DOByear, int DOBmonth, int DOBday) {

			int age;

			final Calendar calenderToday = Calendar.getInstance();
			int currentYear = calenderToday.get(Calendar.YEAR);
			int currentMonth = 1 + calenderToday.get(Calendar.MONTH);
			int todayDay = calenderToday.get(Calendar.DAY_OF_MONTH);

			age = currentYear - DOByear;

			/*if(DOBmonth > currentMonth){
				--age;
			}
			else if(DOBmonth == currentMonth){
				if(DOBday > todayDay){
					--age;
				}
			}*/
			return age;
		}




		public static  void hiddenKeyboard(View v, Context con) {

			InputMethodManager keyboard = (InputMethodManager)con.getSystemService(con.INPUT_METHOD_SERVICE);

			keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);

		}


//		public static void hideKeyBoardInsideFragment() {
//			final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
//		}

		public static String GetSystemTime(){

			String SystemTime = "";
			Calendar cal = Calendar.getInstance();

			int millisecond = cal.get(Calendar.MILLISECOND);
			int second = cal.get(Calendar.SECOND);
			int minute = cal.get(Calendar.MINUTE);
			//12 hour format
			int hour = cal.get(Calendar.HOUR);
			//24 hour format
			int hourofday = cal.get(Calendar.HOUR_OF_DAY);

			SystemTime = hour+":"+minute;
			return SystemTime;
		}

		public static String ReplaceNewLineCodeFromString(String OriginalString){
			String NewString = "";
			if(OriginalString!=null && !OriginalString.equalsIgnoreCase("")){
				if(OriginalString.contains("\n")){

					NewString = OriginalString.replace("\n","");
					OriginalString = NewString;
                    if(OriginalString.contains("&amp;")){

                        NewString = OriginalString.replace("&amp;"," & ");
                    }
				}else if(OriginalString.contains("&amp;")) {

					NewString = OriginalString.replace("&amp;", " & ");
				}else {
				    NewString = OriginalString;
                }




			}


			return NewString;
		}

		public static String ReplaceCommaFromString(String OriginalString){
			String NewString = "";
			if(OriginalString!=null && !OriginalString.equalsIgnoreCase("")){
				if(OriginalString.contains(",")){

					NewString = OriginalString.replace(",","");
					//OriginalString = NewString;

				}else {
					NewString = OriginalString;
				}




			}


			return NewString;
		}

		public static String UrlFormatString(String OriginalString){
			String NewString = "";
			if(OriginalString!=null && !OriginalString.equalsIgnoreCase("")){
				if(OriginalString.contains(" ")){

					NewString = OriginalString.replace(" ","+");

				}else{
                    NewString = OriginalString;
                }
			}
			return NewString;
		}

		public static int GetSystemHours(){

			int Systemhours = 0;
			Calendar cal = Calendar.getInstance();

			int millisecond = cal.get(Calendar.MILLISECOND);
			int second = cal.get(Calendar.SECOND);
			int minute = cal.get(Calendar.MINUTE);
			//12 hour format
			int hour = cal.get(Calendar.HOUR);
			//24 hour format
			int hourofday = cal.get(Calendar.HOUR_OF_DAY);

			Systemhours = hourofday;
			return Systemhours;
		}

		public static int GetSystemMinutes(){

			int Systemminutes = 0;
			Calendar cal = Calendar.getInstance();

			int millisecond = cal.get(Calendar.MILLISECOND);
			int second = cal.get(Calendar.SECOND);
			int minute = cal.get(Calendar.MINUTE);
			//12 hour format
			int hour = cal.get(Calendar.HOUR);
			//24 hour format
			int hourofday = cal.get(Calendar.HOUR_OF_DAY);

			Systemminutes = minute;
			return Systemminutes;
		}

		public static String afterTextChanged(Editable view) {
			String s = null;
			try {
				// The comma in the format specifier does the trick
				s = String.format("%,d", Long.parseLong(view.toString()));
			} catch (NumberFormatException e) {
			}

			return s;
			// Set s back to the view after temporarily removing the text change listener
		}

		public static void LogD(String Tag, String msg) {

			Log.d(Tag, msg);
		}

		public static boolean InsertLogs(Context context,String rdate,String UserId,String FileName,String Message ){
			Boolean result = false;

			try {
				SQLiteDatabase database = context.openOrCreateDatabase("FinPin.db", Context.MODE_PRIVATE, null);
				String query = "INSERT INTO TBL_LOG(LogRegisteredDate,UserId,FileName,Message) VALUES('"+rdate+ "', '"+UserId+"','"+FileName+"','"+Message+"');";
				database.execSQL(query);
				database.close();
				result = true;
			}catch (Exception e){
				e.printStackTrace();
				result = false;
			}
			return result;
		}

        public static void deleteCache(Context context) {
            try {
                File dir = context.getCacheDir();
                deleteDir(dir);
            } catch (Exception e) {}
        }

        public static boolean deleteDir(File dir) {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else if(dir!= null && dir.isFile()) {
                return dir.delete();
            } else {
                return false;
            }
        }



		public static String NumberDisplayFormatting(String NumberToDisplay){

			String FormattedDisplayString = "";
			if(NumberToDisplay!=null && !NumberToDisplay.equalsIgnoreCase("")){
				Double number_dis = Double.valueOf(NumberToDisplay);
				if(number_dis < 10000000 && number_dis > 10000){
					Double new_num = number_dis/100000;
					FormattedDisplayString = String.format("%.2f", new_num) + " Lacs";
				}else if(number_dis > 10000000) {
					Double new_num = number_dis/10000000;
					FormattedDisplayString = String.format("%.2f", new_num) + " Crs";
				}else if(number_dis > 0 && number_dis <= 10000 ){
					try {
						String originalString = NumberToDisplay;

						Long longval;
						if (originalString.contains(",")) {
							originalString = originalString.replaceAll(",", "");
						}
						longval = Long.parseLong(originalString);

						DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
						formatter.applyPattern("#,##,##,##,###");
						FormattedDisplayString = formatter.format(longval);

					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}else if(NumberToDisplay.equalsIgnoreCase("") || number_dis == 0){
					FormattedDisplayString = "0 Lacs";
				}
				}

                /*try {
                    String originalString = NumberToDisplay;

                    Long longval;
                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    longval = Long.parseLong(originalString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,##,##,##,###");
                    FormattedDisplayString = formatter.format(longval);

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }*/
			/*}else if(NumberToDisplay.equalsIgnoreCase("")){
				FormattedDisplayString = "0";
			}*/

			return FormattedDisplayString;
		}

        public static String NumberDisplayFormattingForCompare(String NumberToDisplay){

            String FormattedDisplayString = "";
            if(NumberToDisplay!=null && !NumberToDisplay.equalsIgnoreCase("")){
                Double number_dis = Double.valueOf(NumberToDisplay);
                if(number_dis >= 10000000) {
                    Double new_num = number_dis/10000000;
                    FormattedDisplayString = String.format("%.2f", new_num) + " Crs";
                }else if(number_dis < 10000000 && number_dis >= 100000){
                    Double new_num = number_dis/100000;
                    FormattedDisplayString = String.format("%.2f", new_num) + " L";
                }else if(number_dis >= 1000 && number_dis < 100000){
                    Double new_num = number_dis/1000;
                    FormattedDisplayString = String.format("%.2f", new_num) + " K";

                }else if(number_dis > 0 && number_dis < 1000){
                    try {
                        String originalString = NumberToDisplay;

                        Long longval;
                        if (originalString.contains(",")) {
                            originalString = originalString.replaceAll(",", "");
                        }
                        longval = Long.parseLong(originalString);

                        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                        formatter.applyPattern("#,##,##,##,###");
                        FormattedDisplayString = formatter.format(longval);

                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                }else if(NumberToDisplay.equalsIgnoreCase("") || number_dis <= 0){
                    FormattedDisplayString = "0";
                }
            }

                /*try {
                    String originalString = NumberToDisplay;

                    Long longval;
                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    longval = Long.parseLong(originalString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,##,##,##,###");
                    FormattedDisplayString = formatter.format(longval);

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }*/
			/*}else if(NumberToDisplay.equalsIgnoreCase("")){
				FormattedDisplayString = "0";
			}*/

            return FormattedDisplayString;
        }

		public static String NumberDisplayFormattingForCompareWithNegativeValues(String NumberToDisplay){

        	String FormattedDisplayString = "";
			if(NumberToDisplay!=null && !NumberToDisplay.equalsIgnoreCase("")){
				Double number_dis = Double.valueOf(NumberToDisplay);


				if(number_dis >= 10000000) {
					Double new_num = number_dis/10000000;
					FormattedDisplayString = String.format("%.2f", new_num) + " Crs";
				}else if(number_dis < 10000000 && number_dis >= 100000){
					Double new_num = number_dis/100000;
					FormattedDisplayString = String.format("%.2f", new_num) + " L";
				}else if(number_dis >= 1000 && number_dis < 100000){
					Double new_num = number_dis/1000;
					FormattedDisplayString = String.format("%.2f", new_num) + " K";

				}else if(number_dis > 0 && number_dis < 1000){
					try {
						String originalString = NumberToDisplay;

						Long longval;
						if (originalString.contains(",")) {
							originalString = originalString.replaceAll(",", "");
						}
						longval = Long.parseLong(originalString);

						DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
						formatter.applyPattern("#,##,##,##,###");
						FormattedDisplayString = formatter.format(longval);

					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}else if(NumberToDisplay.equalsIgnoreCase("") || number_dis == 0){
					FormattedDisplayString = "0";
				}else if(number_dis < 0){

				    double abs_value = Math.abs(number_dis);
				    String NewStringFormatted = NumberDisplayFormattingForCompare(String.valueOf(abs_value));
				    FormattedDisplayString = "-"+NewStringFormatted;
                }
			}

                /*try {
                    String originalString = NumberToDisplay;

                    Long longval;
                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    longval = Long.parseLong(originalString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,##,##,##,###");
                    FormattedDisplayString = formatter.format(longval);

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }*/
			/*}else if(NumberToDisplay.equalsIgnoreCase("")){
				FormattedDisplayString = "0";
			}*/

			return FormattedDisplayString;
		}


        public static String NumberDisplayFormattingWithComma(String NumberToDisplay){

			String FormattedDisplayString = "";
			String DecimalTrimmedValue = null;
			if(NumberToDisplay!=null && !NumberToDisplay.equalsIgnoreCase("")){

			    try {

					/*if(NumberToDisplay.contains(".")) {
						String[] TrimmedDecimalresult = NumberToDisplay.split(".");
						DecimalTrimmedValue = TrimmedDecimalresult[0];
					}else {
						DecimalTrimmedValue = NumberToDisplay;
					}*/

                    String originalString = NumberToDisplay;

                    Long longval;
                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    longval = Long.parseLong(originalString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,##,##,##,###");
                    FormattedDisplayString = formatter.format(longval);

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
			}else if(NumberToDisplay.equalsIgnoreCase("")){
				FormattedDisplayString = "0";
			}

			return FormattedDisplayString;
		}

		public static String DecimalNumberDisplayFormattingWithComma(String NumberToDisplay){

			String FormattedDisplayString = "";
			String DecimalTrimmedValue = "";
			if(NumberToDisplay!=null && !NumberToDisplay.equalsIgnoreCase("")){
				String originalString = NumberToDisplay;
				try {



					Double longval;
					if (originalString.contains(",")) {
						originalString = originalString.replaceAll(",", "");
					}

					/*if (originalString.contains(".")) {
						String [] dateParts = originalString.split(".");
						DecimalTrimmedValue = dateParts[0];
						originalString = DecimalTrimmedValue;
					}
*/
					longval = Double.parseDouble(originalString);

					DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
					formatter.applyPattern("#,##,##,##,###.00");
					FormattedDisplayString = formatter.format(longval);

					if(FormattedDisplayString.equalsIgnoreCase(".00")){
						FormattedDisplayString = "0.00";
					}


				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					FormattedDisplayString = originalString;
				}
			}else if(NumberToDisplay.equalsIgnoreCase("")){
				FormattedDisplayString = "0";
			}

			return FormattedDisplayString;
		}



		public static String JotzDisplayDateFormat(String YMDFormatDate){
			String FormattedString = "";
            String yrs,mnts,days;
            int new_year,new_month,new_day;

			if(!YMDFormatDate.equalsIgnoreCase("")){


                String[] parts = YMDFormatDate.split("-");
                yrs = parts[0];
                mnts = parts[1];
                days = parts[2];
                new_year = Integer.parseInt(yrs);
                new_month = Integer.parseInt(mnts);
                new_day = Integer.parseInt(days);

                String Month_initials = GetMonthInitials(new_month);

                FormattedString = Month_initials.toUpperCase() + " " + yrs;
            }


			return FormattedString;
		}


		public  void TruncateTableLogs(Context context){
			SQLiteDatabase database = context.openOrCreateDatabase("FinPin.db", Context.MODE_PRIVATE, null);
			String query = "DELETE FROM  TBL_LOG;";
			database.execSQL(query);
			database.close();
		}

		private static String getDate(Context context,long milliSeconds) {
			// Create a DateFormatter object for displaying date in specified
			// format.
			SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
			// Create a calendar object that will convert the date and time value in
			// milliseconds to date.
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis((int) milliSeconds);
			return formatter.format(calendar.getTime());
		}



		public static String SavePrefrence(Context context, String key, String value)
			{
				SharedPreferences preferences = context.getSharedPreferences("USERDETAIL", 0);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(key, value);
				editor.commit();
				return key;
			}
		public static String LoadPrefrence(Context context, String key)
			{
				SharedPreferences preferences = context.getSharedPreferences("USERDETAIL", 0);
				String value = preferences.getString(key, "");
				return value;
			}
		public static boolean createDirIfNotExists(String path) {
		    boolean ret = true;

		    File file = new File(Environment.getExternalStorageDirectory(), path);
		    if (!file.exists()) {
		        if (!file.mkdirs()) {
		           // Log.e("TravellerLog :: ", "Problem creating Image folder");
		            ret = false;
		        }
		    }
		    return ret;
		}
		public static boolean isSDPresent()
			{
				return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
			}
		
		public static void setTextViewFont(Context context, TextView textView)
		{
			try
				{
					Typeface tf = Typeface.createFromAsset(context.getAssets(), "font/roboto_light.ttf");
					textView.setTypeface(tf);
					//textView.setTextColor(Color.BLACK);
				}
			catch (Exception e)
				{
					e.toString();
				}
		}
		public static void setButtonFont(Context context, Button button)
		{
			try
				{
					Typeface tf = Typeface.createFromAsset(context.getAssets(), "font/roboto_bold.ttf");
					button.setTypeface(tf);
					
					
				}
			catch (Exception e)
				{
					e.toString();
				}
		}

		public static int CalculateMonth(String StartDate,String EndDate){

			/*Noteeeee ---->  StartDate & EndDate passed to this function should be in yyyy-MM-DD format only*/

			int total_months= 0;
			int sd_dd,sd_mm,sd_yy,ed_dd,ed_mm,ed_yy;
			String[] parts = StartDate.split("-");
			String yrsSD_Lumpsum = parts[0];
			String mntsSD_Lumpsum = parts[1];
			String daysSD_Lumpsum = parts[2];
			sd_yy = Integer.parseInt(yrsSD_Lumpsum);
			sd_mm = Integer.parseInt(mntsSD_Lumpsum)-1;
			sd_dd = Integer.parseInt(daysSD_Lumpsum);

			String[] partsss = EndDate.split("-");
			String yrsED_Lumpsum = partsss[0];
			String mntsED_Lumpsum = partsss[1];
			String daysED_Lumpsum = partsss[2];
			ed_yy = Integer.parseInt(yrsED_Lumpsum);
			ed_mm = Integer.parseInt(mntsED_Lumpsum)-1;
			ed_dd = Integer.parseInt(daysED_Lumpsum);


			total_months = ((ed_yy - sd_yy)*12+(ed_mm - sd_mm))+1;
			return total_months;
		}

		public static String DateDisplayedFormat(String DateIn_YMD){

			String finalDate = null;

			try {
				finalDate = new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(DateIn_YMD));
			} catch (ParseException e) {
				Log.d("Exception", e.toString());
			}

			return finalDate;
		}

        public static String GetMonthInitials(int MonthCode){


            if(MonthCode==1){
                return "Jan";
            }else if(MonthCode==2){
                return "Feb";
            }else if(MonthCode==3){
                return "Mar";
            }else if(MonthCode==4){
                return "Apr";
            }else if(MonthCode==5){
                return "May";
            }else if(MonthCode==6){
                return "Jun";
            }else if(MonthCode==7){
                return "Jul";
            }else if(MonthCode==8){
                return "Aug";
            }else if(MonthCode==9){
                return "Sep";
            }else if(MonthCode==10){
                return "Oct";
            }else if(MonthCode==11){
                return "Nov";
            }else if(MonthCode==12){
                return "Dec";
            }else {
                return "month";
            }


        }



        public static String GetMonth(int MonthCode){


			if(MonthCode==1){
				return "January";
			}else if(MonthCode==2){
				return "February";
			}else if(MonthCode==3){
				return "March";
			}else if(MonthCode==4){
				return "April";
			}else if(MonthCode==5){
				return "May";
			}else if(MonthCode==6){
				return "June";
			}else if(MonthCode==7){
				return "July";
			}else if(MonthCode==8){
				return "August";
			}else if(MonthCode==9){
				return "September";
			}else if(MonthCode==10){
				return "October";
			}else if(MonthCode==11){
				return "November";
			}else if(MonthCode==12){
				return "December";
			}else {
				return "month";
			}


		}



		public static Integer GetMonthCode(String Month){

            Integer month_code= 0;

            if(Month.equalsIgnoreCase("January")){
                month_code =  1;
            }else if(Month.equalsIgnoreCase("February")){
                month_code = 2;
            }else if(Month.equalsIgnoreCase("March")){
                month_code = 3 ;
            }else if(Month.equalsIgnoreCase("April")){
                month_code = 4 ;
            }else if(Month.equalsIgnoreCase("May")){
                month_code = 5 ;
            }else if(Month.equalsIgnoreCase("June")){
               month_code = 6 ;
            }else if(Month.equalsIgnoreCase("July")){
                month_code = 7 ;
            }else if(Month.equalsIgnoreCase("August")){
                month_code = 8 ;
            }else if(Month.equalsIgnoreCase("September")){
                month_code = 9 ;
            }else if(Month.equalsIgnoreCase("October")){
                month_code = 10 ;
            }else if(Month.equalsIgnoreCase("November")){
                month_code = 11 ;
            }else if(Month.equalsIgnoreCase("December")){
                month_code = 12 ;
            }
            return  month_code;
        }

		public static String DisplayCurrentDate() {
			String CurrentDate = null;
			SimpleDateFormat formDate = new SimpleDateFormat("dd-MM-yyyy");

			// String strDate = formDate.format(System.currentTimeMillis()); // option 1
			 CurrentDate = formDate.format(new Date(System.currentTimeMillis()-24*60*60*1000)); // option 2
		     return CurrentDate;
		}

		public static String GetOneDayLessDate(String YMD_Date){
			String OneDayLessDate = null;
			String dateParts[];
			SimpleDateFormat formDate = new SimpleDateFormat("yyyy-MM-dd");
			if(YMD_Date!=null && !YMD_Date.equals("")){
				dateParts = YMD_Date.split("-");
				String day = dateParts[2];
				String month = dateParts[1];
				String year = dateParts[0];
				if(!day.equalsIgnoreCase("01")||!day.equalsIgnoreCase("1")) {
					int OneDayless = Integer.valueOf(day) - 1;
					String NewDate = dateParts[0] + "-" + dateParts[1] + "-" + String.valueOf(OneDayless);
					OneDayLessDate = NewDate;
				}else{
					if(!month.equalsIgnoreCase("01")||!month.equalsIgnoreCase("1")){
						int OneDayless = 30;
						int OneMonthless = Integer.valueOf(month)-1;
						String NewDate = dateParts[0] + "-" +String.valueOf(OneMonthless) + "-" + String.valueOf(OneDayless);
						OneDayLessDate = NewDate;
					}else {
						int OneDayless = 30;
						int OneMonthless = Integer.valueOf(month)-1;
						int OneYearless = Integer.valueOf(year);
						String NewDate = String.valueOf(OneYearless) + "-" +String.valueOf(OneMonthless) + "-" + String.valueOf(OneDayless);
						OneDayLessDate = NewDate;
					}
				}
			}


			return OneDayLessDate;



		}

		public static String GetDisplayFormattedDate(String nav_date){
			String finalDate = null;

			if(nav_date!=null && !nav_date.equalsIgnoreCase("")||!nav_date.equalsIgnoreCase("null")){

				String myDate = nav_date;
				String strMnth    = myDate.substring(0,3);
				String day        = myDate.substring(4,6);
				String year       = myDate.substring(8,12);
				String strMonth = null;

				if(strMnth.equalsIgnoreCase("Jan")){
					strMonth = "01";
				}if(strMnth.equalsIgnoreCase("Feb")){
					strMonth = "02";
				}if(strMnth.equalsIgnoreCase("Mar")){
					strMonth = "03";
				}if(strMnth.equalsIgnoreCase("Apr")){
					strMonth = "04";
				}if(strMnth.equalsIgnoreCase("May")){
					strMonth = "05";
				}if(strMnth.equalsIgnoreCase("Jun")){
					strMonth = "06";
				}if(strMnth.equalsIgnoreCase("Jul")){
					strMonth = "07";
				}if(strMnth.equalsIgnoreCase("Aug")){
					strMonth = "08";
				}if(strMnth.equalsIgnoreCase("Sep")){
					strMonth = "09";
				}if(strMnth.equalsIgnoreCase("Oct")){
					strMonth = "10";
				}if(strMnth.equalsIgnoreCase("Nov")){
					strMonth = "11";
				}if(strMnth.equalsIgnoreCase("Dec")){
					strMonth = "12";
				}
				finalDate = day+"/"+strMonth+'/'+year;
				Log.d("StrDate",""+finalDate);


			}

			return finalDate;
		}


		public static int GetCurrentMonth(){

			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			return  month;
		}

		public static int GetCurrentYear(){

			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);

			return  year;
		}



        public static void clearForm(ViewGroup group) {
            for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                View view = group.getChildAt(i);
                if (view instanceof EditText) {
                    ((EditText)view).setText("");
                }

                if(view instanceof ViewGroup && (((ViewGroup)view).getChildCount() > 0))
                    clearForm((ViewGroup)view);
            }
        }

		public static String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
			byte[] byteFormat = stream.toByteArray();
			// get the base 64 string
			String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

			return imgString;
		}

        public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
            int width = image.getWidth();
            int height = image.getHeight();

            float bitmapRatio = (float)width / (float) height;
            if (bitmapRatio > 1) {
                width = maxSize;
                height = (int) (width / bitmapRatio);
            } else {
                height = maxSize;
                width = (int) (height * bitmapRatio);
            }
            return Bitmap.createScaledBitmap(image, width, height, true);
        }

	}
