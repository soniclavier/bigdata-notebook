package weka.filters;

import weka.core.*;
import weka.core.Capabilities.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by vishnu on 6/20/16.
 */
public class AnimalShelterPreprocess extends SimpleStreamFilter implements OptionHandler{


    protected int nameIndex = 1;
    protected int ageUponoutcomeIndex = 5;
    protected int breedIndex = 6;
    protected int dateTimeIndex = 2;
    protected int colorIndex = 7;
    protected int sexUponOutcomeIndex = 4;

    int noNameIndex = 0;
    int ageCatIndex = 0;
    int sexIndex = 0;
    int seasonIndex = 0;

    int numNewAttrib = 0;

    static final String DATE_FORMAT = "MM/dd/yy HH:mm";
    static final String YOUNG = "young";
    static final String TEEN = "teen";
    static final String ADULT = "adult";
    static final String OLD = "old";
    static final String BABY = "baby";


    static final String WINTER = "winter";
    static final String SUMMER = "summer";
    static final String SPRING = "spring";
    static final String FALL = "fall";

    static final String YES = "yes";
    static final String NO = "no";

    static final String MALE = "male";
    static final String FEMALE = "female";
    static final String UNKNOWN = "unknown";

    static Map<String,String> breedValuesMap = new HashMap<String,String>();
    static Set<String> newBreedValues = new HashSet<String>();
    static Map<String,String> colorValuesMap = new HashMap<String,String>();
    static Set<String> newColorValues = new HashSet<String>();
    static boolean initSetup = false;
    static Instances result;

    /**
     * 1. normalize ageUponOutcome to days
     * 2. create a new filed called, no_name and set it to true or false
     * @return
     */
    public Enumeration<Option> listOptions() {
        Vector result = new Vector(1);
        result.addElement(new Option("\tSpecify the index of the name attribute", "N", nameIndex, "-N <num>"));
        result.addElement(new Option("\tSpecify the index of the ageUponOutcome attribute", "A", ageUponoutcomeIndex, "-A <num>"));
        result.addElement(new Option("\tSpecify the index of the breed attribute", "B", breedIndex, "-B <num>"));
        result.addElement(new Option("\tSpecify the index of the color attribute", "C", colorIndex, "-C <num>"));
        result.addElement(new Option("\tSpecify the index of the dateTime attribute", "D", dateTimeIndex, "-D <num>"));
        result.addElement(new Option("\tSpecify the index of the sexuponoutcome attribute", "S", sexUponOutcomeIndex, "-S <num>"));
        return result.elements();
    }

    public void setOptions(String[] options) throws Exception {
        String tmpStr = Utils.getOption('A', options);
        if(tmpStr.length() != 0) {
            this.setAgeUponoutcomeIndex(Integer.parseInt(tmpStr));
        } else {
            this.setAgeUponoutcomeIndex(ageUponoutcomeIndex);
        }

        tmpStr = Utils.getOption('N', options);
        if(tmpStr.length() != 0) {
            this.setNameIndex(Integer.parseInt(tmpStr));
        } else {
            this.setNameIndex(nameIndex);
        }

        tmpStr = Utils.getOption('B', options);
        if(tmpStr.length() != 0) {
            this.setBreedIndex(Integer.parseInt(tmpStr));
        } else {
            this.setBreedIndex(breedIndex);
        }

        tmpStr = Utils.getOption('C', options);
        if(tmpStr.length() != 0) {
            this.setColorIndex(Integer.parseInt(tmpStr));
        } else {
            this.setColorIndex(colorIndex);
        }

        tmpStr = Utils.getOption('D', options);
        if(tmpStr.length() != 0) {
            this.setDateTimeIndex(Integer.parseInt(tmpStr));
        } else {
            this.setDateTimeIndex(dateTimeIndex);
        }

        tmpStr = Utils.getOption('S', options);
        if(tmpStr.length() != 0) {
            this.setSexUponOutcomeIndex(Integer.parseInt(tmpStr));
        } else {
            this.setSexUponOutcomeIndex(dateTimeIndex);
        }


        if(this.getInputFormat() != null) {
            this.setInputFormat(this.getInputFormat());
        }
        Utils.checkForRemainingOptions(options);
    }

    public String[] getOptions() {
        Vector result = new Vector();
        result.add("-A");
        result.add("" + this.getAgeUponoutcomeIndex());
        result.add("-N");
        result.add("" + this.getNameIndex());
        result.add("-B");
        result.add("" + this.getBreedIndex());
        result.add("-C");
        result.add("" + this.getColorIndex());
        result.add("-D");
        result.add("" + this.getDateTimeIndex());
        result.add("-S");
        result.add("" + this.getSexUponOutcomeIndex());


        return (String[])result.toArray(new String[result.size()]);
    }

    public void setAgeUponoutcomeIndex(int ageUponoutcomeIndex) {
        this.ageUponoutcomeIndex = ageUponoutcomeIndex;
    }

    public int getAgeUponoutcomeIndex() {
        return this.ageUponoutcomeIndex;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    public int getBreedIndex() {
        return breedIndex;
    }

    public void setBreedIndex(int breedIndex) {
        this.breedIndex = breedIndex;
    }


    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public int getDateTimeIndex() {
        return dateTimeIndex;
    }

    public void setDateTimeIndex(int dateTimeIndex) {
        this.dateTimeIndex = dateTimeIndex;
    }

    public int getSexUponOutcomeIndex() {
        return sexUponOutcomeIndex;
    }

    public void setSexUponOutcomeIndex(int sexUponOutcomeIndex) {
        this.sexUponOutcomeIndex = sexUponOutcomeIndex;
    }

    @Override
    public String globalInfo() {
        return "Normalizes age by converting into unit of days, adds a new attribute normalized_age";
    }

    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.enableAllAttributes();
        result.enableAllClasses();
        result.enable(Capability.NO_CLASS);
        return result;

    }

    @Override
    protected Instances determineOutputFormat(Instances instances) throws Exception {

        if (!initSetup) {
            result = new Instances(instances, 0);

            Enumeration<Object> enumeration = result.attribute(breedIndex).enumerateValues();

            while (enumeration.hasMoreElements()) {
                String breedValue = (String) enumeration.nextElement();
                breedValue = breedValue.trim();
                String newBreedValue = breedValue;
                int ind = breedValue.indexOf("/");
                if (ind != -1) {
                    newBreedValue = breedValue.substring(0, ind);
                    /*
                    String temp = breedValue.substring(ind);
                    int ind2 = temp.indexOf(" ");
                    if (ind2 != -1) {
                        newBreedValue = newBreedValue +temp.substring(ind2+1);
                    }*/
                }
                ind = newBreedValue.indexOf("Mix");
                if (ind != -1) {
                    newBreedValue = newBreedValue.substring(0, ind);
                }
                newBreedValue = newBreedValue.trim();

                breedValuesMap.put(breedValue,newBreedValue);
            }
            for(String value : breedValuesMap.values()) {
                newBreedValues.add(value);
            }

            enumeration = result.attribute(colorIndex).enumerateValues();

            while (enumeration.hasMoreElements()) {
                String colorValue = (String) enumeration.nextElement();
                colorValue = colorValue.trim();
                String newColorValue = colorValue;
                int ind = colorValue.indexOf("/");
                if (ind != -1) {
                    newColorValue = colorValue.substring(0, ind);
                }
                ind = newColorValue.indexOf(" ");
                if (ind != -1) {
                    newColorValue = newColorValue.substring(0, ind);
                }
                newColorValue = newColorValue.trim();

                colorValuesMap.put(colorValue,newColorValue);
            }

            for(String value : colorValuesMap.values()) {
                newColorValues.add(value);
            }


            System.out.println("should be here only once ");
            System.out.println("number of breed values = "+ breedValuesMap.size());

            int nextAttInd = result.numAttributes();
            if (ageUponoutcomeIndex >= 0) {
                result.replaceAttributeAt(new Attribute("Normalized_age"), ageUponoutcomeIndex);
                List<String> ageCat = new ArrayList<String>();
                ageCat.add(BABY);
                ageCat.add(YOUNG);
                ageCat.add(ADULT);
                ageCat.add(OLD);
                ageCat.add(TEEN);
                result.insertAttributeAt(new Attribute("Age_Category",ageCat),nextAttInd);
                ageCatIndex = nextAttInd;
                nextAttInd++;
                numNewAttrib++;
            }
            if (noNameIndex >= 0) {
                List<String> hasNameCat = new ArrayList<String>();
                hasNameCat.add(YES);
                hasNameCat.add(NO);
                result.insertAttributeAt(new Attribute("Has_Name",hasNameCat),nextAttInd);
                noNameIndex = nextAttInd;
                nextAttInd++;
                numNewAttrib++;
            }


            if (breedIndex >= 0) result.replaceAttributeAt(new Attribute("Breed",new ArrayList<String>(newBreedValues)),breedIndex);
            if (colorIndex >= 0) result.replaceAttributeAt(new Attribute("Color",new ArrayList<String>(newColorValues)),colorIndex);

            if (dateTimeIndex >= 0) {
                List<String> seasonCat = new ArrayList<String>();
                seasonCat.add(WINTER);
                seasonCat.add(SUMMER);
                seasonCat.add(SPRING);
                seasonCat.add(FALL);
                //seasonCat.add(UNKNOWN);
                result.insertAttributeAt(new Attribute("Season",seasonCat),nextAttInd);
                seasonIndex = nextAttInd;
                numNewAttrib++;
                nextAttInd++;
            }

            if (sexUponOutcomeIndex >=0){
                List<String> sex = new ArrayList<String>();
                sex.add(MALE);
                sex.add(FEMALE);
                sex.add(UNKNOWN);
                result.insertAttributeAt(new Attribute("Sex",sex),nextAttInd);
                sexIndex = nextAttInd;
                nextAttInd++;
                numNewAttrib++;
            }
            initSetup = true;

        }
        return result;
    }

    @Override
    protected Instance process(Instance instance) throws Exception {

        //double[] values = new double[instance.numAttributes()+numNewAttrib];
        Instance ins = new DenseInstance(instance.numAttributes()+numNewAttrib);
        ins.setDataset(determineOutputFormat(instance.dataset()));
        try {
            for (int n = 0; n < instance.numAttributes(); n++) {

                if (n == ageUponoutcomeIndex) {
                    String currentVal = instance.stringValue(n);
                    if (currentVal.equals("?")) currentVal = "797 days";
                    String[] parts = currentVal.split(" ");
                    int mul = 1;
                    if (parts[1].endsWith("s")) parts[1] = parts[1].substring(0, parts[1].length() - 1);
                    if (parts[1].equals("month")) {
                        mul = 30;
                    } else if (parts[1].equals("year")) {
                        mul = 365;
                    } else if (parts[1].equals("week")) {
                        mul = 7;
                    }
                    //values[n] = Double.parseDouble(parts[0])*mul;
                    double age = Double.parseDouble(parts[0]) * mul;
                    String ageCat = ADULT;
                    if (age < 60) {
                        ageCat = BABY;
                    }
                    else if (age < 1000)
                        ageCat = YOUNG;
                    else if (age >= 1000 && age <= 1800) {
                        ageCat = TEEN;
                    }
                    else if (age >1800 && age > 3200.0)
                        ageCat = OLD;
                    ins.setValue(n, age);
                    ins.setValue(ageCatIndex,ageCat);

                } else if (n == nameIndex) {
                    String noNameVal = NO;
                    if (instance.stringValue(n).trim().equals("?"))
                        noNameVal = YES;
                    //values[noNameIndex] = noNameVal;
                    //values[n] = instance.value(n);
                    ins.setValue(noNameIndex, noNameVal);
                    ins.setValue(n, instance.value(n));
                } else if (n == breedIndex) {
                    String breed = instance.stringValue(n).trim();
                    ins.setValue(n, breedValuesMap.get(breed));

                } else if (n == colorIndex) {
                    String color = instance.stringValue(n).trim();
                    ins.setValue(n, colorValuesMap.get(color));
                } else if (n == sexUponOutcomeIndex) {
                    String sexUponOutcome = instance.stringValue(n).trim();
                    String sex = UNKNOWN;
                    if (sexUponOutcome.contains("Male"))
                        sex = MALE;
                    else if (sexUponOutcome.contains("Female"))
                        sex = FEMALE;
                    ins.setValue(sexIndex,sex);
                    ins.setValue(n,instance.value(n));
                }else if (n == dateTimeIndex) {
                    String dateTime = instance.stringValue(n);
                    String season = getSeason(dateTime);
                    ins.setValue(seasonIndex,season);
                    ins.setValue(n,instance.value(n));
                } else{
                    //values[n] = instance.value(n);
                    ins.setValue(n, instance.value(n));
                }
            }
        }catch(Exception e) {
           //System.out.println(e.toString());
            e.printStackTrace();
        }
        //Instance result = new DenseInstance(1, values);
        //System.out.println("The instance: " + ins);
        return ins;
    }

    public String getSeason(String datestr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date d = sdf.parse(datestr);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            int month = c.get(Calendar.MONTH);
            month++;
            if (month >= 12 || month<=2)
                return WINTER;
            if (month >=3 && month <=5)
                return SPRING;
            if (month >= 6 && month <=8)
                return SUMMER;
            if (month >=9 && month <= 11)
                return FALL;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return UNKNOWN;
    }

    public static void main(String[] args) {

        runFilter(new AnimalShelterPreprocess(), args);

    }


}
