package model;

public class Budget {

    private int id;
    private int userId;
    private double amount;
    private int month;
    private int year;

    public Budget() {
    }

    public Budget(int userId, double amount, int month, int year) {
        this.userId = userId;
        this.amount = amount;
        this.month = month;
        this.year = year;
    }

    public Budget(int id, int userId, double amount, int month, int year) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.month = month;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}