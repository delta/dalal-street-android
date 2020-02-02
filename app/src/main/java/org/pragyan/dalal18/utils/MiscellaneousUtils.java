package org.pragyan.dalal18.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MiscellaneousUtils {

    public static String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss";
        String outputPattern = "MMM dd hh:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.US);

        String str = null;

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputFormat.parse(time));
            str = outputFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String sessionId = "dalalStreetSessionId";
    public static String username = null;

    /* Main Server Certificate */

    // TODO (Release) : Update server crt with main server
    public static final String SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDjzCCAnegAwIBAgIJAON0T+GsNoM0MA0GCSqGSIb3DQEBCwUAMF4xCzAJBgNV\n" +
            "BAYTAklOMQ8wDQYDVQQHDAZUcmljaHkxDjAMBgNVBAoMBURlbHRhMRowGAYDVQQL\n" +
            "DBFEYWxhbCBTdHJlZXQgVGVhbTESMBAGA1UEAwwJbG9jYWxob3N0MB4XDTE3MTIy\n" +
            "NjIwMzIwNVoXDTI3MTIyNDIwMzIwNVowXjELMAkGA1UEBhMCSU4xDzANBgNVBAcM\n" +
            "BlRyaWNoeTEOMAwGA1UECgwFRGVsdGExGjAYBgNVBAsMEURhbGFsIFN0cmVldCBU\n" +
            "ZWFtMRIwEAYDVQQDDAlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw\n" +
            "ggEKAoIBAQCyfpFvDTbonf0U5dwhEQ2yCMvj6gFyq7gEYD0Ch9S2WcO9zo70uVBI\n" +
            "zvKc+gJ4/OHzmlEP33+x2Q9LC9H6pTwWO+ZltM6/u7z7IuI3BAv2O5a7KqAh0B16\n" +
            "nEv6C+47S9eATUgbtalNpbpCEqBJB/zr+vfoAv4k76G840wHA3NJYyGRJ/sfOrPs\n" +
            "ks8TnMCpUIn2HKAaihXBDjx91HCKdkf1RIqBGADN1s6Q75xJq+VjO39BEKs1Wh+C\n" +
            "cg3l8rQ9FotKlVYnfgfpEL5qouY9BgneXsb+ixMBkSCoZlaiJpaEYUZQ9CjbIECy\n" +
            "KL34JEDkNQlGFHI4dnxs8RJtBcVVaYs9AgMBAAGjUDBOMB0GA1UdDgQWBBS9CU6m\n" +
            "LHaqYFG9rvpl1aTwZyvnzzAfBgNVHSMEGDAWgBS9CU6mLHaqYFG9rvpl1aTwZyvn\n" +
            "zzAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBp8GM4Ne4sPtDBb6QZ\n" +
            "IWO+fKQkFvSCvD0TXTUxU88QGWzOMyVlCskQo0P1/vjPIylRTVAJWC9J164PnKW2\n" +
            "U7RYgKFVGIfdQmkD6Ul2ip2XVJ7awoXjQO51nNp1uN9UzsHaIoswPe5KcJ/TOZ75\n" +
            "8BByY+l/L8EOXdk+DTq2kPS1mcv6a500Q9JTDAfAM6uUVnwiF4fWtfSUyyRpWl6s\n" +
            "xY01aSxRMQud7e8h/FDMPdTvcFpcnWUB2byRdw7gv5kIjfRsZZSwy/8zODZx/biR\n" +
            "n8lG/ZKeivbUai547FHqdI2qJwv2mElxojG2hLV7sImSg26fuaTyy5+ftP3GH8Yj\n" +
            "5+8T\n" +
            "-----END CERTIFICATE-----\n";


    /*public static String[] country = new String[]{"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla",

            "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria",

            "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",

            "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana",

            "Brazil", "British Indian Ocean Territory", "British Virgin Islands", "Brunei", "Bulgaria",

            "Burkina Faso", "Burma (Myanmar)", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde",

            "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island",

            "Cocos (Keeling) Islands", "Colombia", "Comoros", "Cook Islands", "Costa Rica",

            "Croatia", "Cuba", "Cyprus", "Czech Republic", "Democratic Republic of the Congo",

            "Denmark", "Djibouti", "Dominica", "Dominican Republic",

            "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia",

            "Ethiopia", "Falkland Islands", "Faroe Islands", "Fiji", "Finland", "France", "French Polynesia",

            "Gabon", "Gambia", "Gaza Strip", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece",

            "Greenland", "Grenada", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana",

            "Haiti", "Holy See (Vatican City)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India",

            "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Ivory Coast", "Jamaica",

            "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kosovo", "Kuwait",

            "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein",

            "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", "Malaysia",

            "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mayotte", "Mexico",

            "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco",

            "Mozambique", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia",

            "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Korea",

            "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama",

            "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn Islands", "Poland",

            "Portugal", "Puerto Rico", "Qatar", "Republic of the Congo", "Romania", "Russia", "Rwanda",

            "Saint Barthelemy", "Saint Helena", "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin",

            "Saint Pierre and Miquelon", "Saint Vincent and the Grenadines", "Samoa", "San Marino",

            "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone",

            "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Korea",

            "Spain", "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland",

            "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tokelau",

            "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands",

            "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "US Virgin Islands", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam",

            "Wallis and Futuna", "West Bank", "Yemen", "Zambia", "Zimbabwe"};

    public static String[]  code = new String[]{"+93", "+355", "+213", "+684", "+376", "+244", "+1 264", "+672", "+1 268", "+54", "+374",

            "+297", "+61", "+43", "+994", "+1 242", "+973", "+880", "+1 246", "+375", "+32", "+501",

            "+229", "+1 441", "+975", "+591", "+387", "+267", "+55", "+246", "+1 284", "+673", "+359",

            "+226", "+95", "+257", "+855", "+237", "+1", "+238", "+1 345", "+236", "+235", "+56", "+86",

            "+61", "+891", "+57", "+269", "+682", "+506", "+385", "+53", "+357", "+420", "+243", "+45",

            "+253", "+1 767", "+1 849", "+1 829", "+1 809", "+593", "+20", "+503", "+240", "+291", "+372",

            "+251", "+500", "+298", "+679", "+358", "+33", "+689", "+241", "+220", "+970", "+995", "+49",

            "+233", "+350", "+30", "+299", "+1 473", "+1 671", "+502", "+224", "+245", "+592", "+509",

            "+379", "+504", "+852", "+36", "+354", "+91", "+62", "+98", "+964", "+353", "+44", "+972",

            "+39", "+225", "+1 876", "+81", "+44", "+962", "+7", "+254", "+686", "+381", "+965", "+996",

            "+856", "+371", "+961", "+266", "+231", "+218", "+423", "+370", "+352", "+853", "+389",

            "+261", "+265", "+60", "+960", "+223", "+356", "+692", "+222", "+230", "+262", "+52", "+691",

            "+373", "+377", "+976", "+382", "+1 664", "+212", "+258", "+264", "+674", "+977", "+31",

            "+599", "+687", "+64", "+505", "+227", "+234", "+683", "+672", "+850", "+1 670", "+47",

            "+968", "+92", "+680", "+507", "+675", "+595", "+51", "+63", "+870", "+48", "+351", "+1",

            "+974", "+242", "+40", "+7", "+250", "+590", "+290", "+1 869", "+1 758", "+1 599", "+508",

            "+1 784", "+685", "+378", "+239", "+966", "+221", "+381", "+248", "+232", "+65", "+421",

            "+386", "+677", "+252", "+27", "+82", "+34", "+94", "+249", "+597", "+268", "+46", "+41",

            "+963", "+886", "+992", "+255", "+66", "+670", "+228", "+690", "+676", "+1 868", "+216",

            "+90", "+993", "+1 649", "+688", "+256", "+380", "+971", "+44", "+1", "+598", "+1 340",

            "+998", "+678", "+58", "+84", "+681", "+970", "+967", "+260", "+263"};

    public String[] getCountry() {
        return country;
    }

    public String[] getCountryCode() {
        return code;
    }*/
}
