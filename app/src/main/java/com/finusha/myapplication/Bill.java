package com.finusha.myapplication;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Bill implements Serializable {
    private String id;
    private String billName;
    private String amountDue;
    private String dueDate;

    public Bill() {
        // Default constructor required for Firebase database operations
    }

    public Bill(String id, String billName, String amountDue, String dueDate) {
        this.id = id;
        this.billName = billName;
        this.amountDue = amountDue;
        this.dueDate = dueDate;
    }

    public String getId() {
        return id;
    }

    public String getBillName() {
        return billName;
    }

    public String getAmountDue() {
        return amountDue;
    }

    public String getDueDate() {
        return dueDate;
    }

    public Date getDueDateAsDate() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.parse(dueDate);
    }

    // Comparator for sorting bills by due date in ascending order
    public static Comparator<Bill> sortByDueDateAsc = new Comparator<Bill>() {
        public int compare(Bill b1, Bill b2) {
            try {
                Date date1 = b1.getDueDateAsDate();
                Date date2 = b2.getDueDateAsDate();
                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }
    };

    @Override
    public String toString() {
        return "ID: " + id + "\nName: " + billName + "\nAmount: " + amountDue + "\nDue Date: " + dueDate;
    }
}
