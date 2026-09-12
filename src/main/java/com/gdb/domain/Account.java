package com.gdb.domain;


public class Account {
    
    private String accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;
    public Account(String accountNumber, String name, int age, double balance, 
                   String accountType, String status) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.age = age;
        this.balance = balance;
        this.accountType = accountType;
        this.status = status;
    }
    
 
    public boolean deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            return true;
        }
        return false;
    }
    
    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
    

    public void displayAccountInfo() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Balance: Rs " + balance);
        System.out.println("Account Type: " + accountType);
        System.out.println("Status: " + status);
    }
    

    public String getAccountNumber() {
        return accountNumber;
    }
    

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
    

    public double getBalance() {
        return balance;
    }
    

    public String getAccountType() {
        return accountType;
    }
    

    public String getStatus() {
        return status;
    }
    

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
