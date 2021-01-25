package vending;

import vending.exceptions.NotFullPaidException;
import vending.exceptions.NotSufficientChangeException;
import vending.exceptions.SoldOutException;

import java.util.ArrayList;
import java.util.List;

public class VendingMachineImpl implements VendingMachine {

    private final Inventory<Coin> cashInventory = new Inventory<>();
    private final Inventory<Item> itemInventory = new Inventory<>();
    private long totalSales;
    private Item currentItem;
    private long currentBalance;

    public VendingMachineImpl() {
        initialize();
    }

    private void initialize() {
        for(Coin c : Coin.values()) {
            cashInventory.put(c, 5);
        }

        for(Item i : Item.values()) {
            itemInventory.put(i, 5);
        }
    }

    @Override
    public long selectItemAndGetPrice(Item item) {
        if(itemInventory.hasItem(item)) {
            currentItem = item;
            return currentItem.getPrice();
        }
        throw new SoldOutException("Sold Out, Please buy another item");
    }

    @Override
    public void insertCoin(Coin coin) {
        currentBalance += coin.getDenomination();
        cashInventory.add(coin);
    }

    @Override
    public List<Coin> refund() {
        List<Coin> refund = getChange(currentBalance);
        updateCashInventory(refund);
        currentBalance = 0;
        currentItem = null;
        return refund;
    }

    @Override
    public Bucket<Item, List<Coin>> collectItemAndChange() {
        Item item = collectItem();
        totalSales += currentItem.getPrice();
        List<Coin> change = collectChange();
        return new Bucket<>(item, change);
    }

    private Item collectItem() throws NotSufficientChangeException, NotFullPaidException {
        if(isFullPaid()) {
            if(hasSufficientChange()) {
                itemInventory.deduct(currentItem);
                return currentItem;
            }
            throw new NotSufficientChangeException("Not Sufficient change in Inventory");
        }
        long remainingBalance = currentItem.getPrice() - currentBalance;
        throw new NotFullPaidException("Price not full paid, remaining : " + remainingBalance);
    }

    private List<Coin> collectChange() {
        long changeAmount = currentBalance - currentItem.getPrice();
        List<Coin> change = getChange(changeAmount);
        updateCashInventory(change);
        currentBalance = 0;
        currentItem = null;
        return change;
    }

    private void updateCashInventory(List<Coin> change) {
        change.forEach(cashInventory::deduct);
    }

    private List<Coin> getChange(long amount) throws NotSufficientChangeException {
        List<Coin> changes = new ArrayList<>();

        if(amount > 0) {
            long balance = amount;
            while(balance > 0) {
                if(balance >= Coin.QUARTER.getDenomination() && cashInventory.hasItem(Coin.QUARTER)) {
                    changes.add(Coin.QUARTER);
                    balance -= Coin.QUARTER.getDenomination();
                }
                else if(balance >= Coin.DIME.getDenomination() && cashInventory.hasItem(Coin.DIME)) {
                    changes.add(Coin.DIME);
                    balance -= Coin.DIME.getDenomination();
                }
                else if(balance >= Coin.NICKLE.getDenomination() && cashInventory.hasItem(Coin.NICKLE)) {
                    changes.add(Coin.NICKLE);
                    balance -= Coin.NICKLE.getDenomination();
                }
                else if(balance >= Coin.PENNY.getDenomination() && cashInventory.hasItem(Coin.PENNY)) {
                    changes.add(Coin.PENNY);
                    balance -= Coin.PENNY.getDenomination();
                }
                else {
                    throw new NotSufficientChangeException("Not Sufficient Change, Please try another product");
                }
            }
        }

        return changes;
    }

    private boolean hasSufficientChange() {
        boolean hasChange = true;
        long amount = currentBalance - currentItem.getPrice();
        try {
            getChange(amount);
        } catch (NotSufficientChangeException e) {
            hasChange = false;
        }
        return hasChange;
    }

    private boolean isFullPaid() {
        return currentBalance >= currentItem.getPrice();
    }

    @Override
    public void reset() {
        cashInventory.clear();
        itemInventory.clear();
        totalSales = 0;
        currentItem = null;
        currentBalance = 0;
    }

    public void printStats() {
        System.out.println("Total Sales : " + totalSales);
        System.out.println("Current Item Inventory : " + itemInventory);
        System.out.println("Current Cash Inventory : " + cashInventory);
    }

    public long getTotalSales() {
        return totalSales;
    }
}
