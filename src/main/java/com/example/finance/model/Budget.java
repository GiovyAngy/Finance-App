package com.example.finance.model;

public class Budget {
    private Long id;
    private String period;
    private double amount;
    private String startDate;
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}