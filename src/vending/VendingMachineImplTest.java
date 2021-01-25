package vending;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vending.exceptions.NotSufficientChangeException;
import vending.exceptions.SoldOutException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VendingMachineImplTest {

    private static VendingMachine vm;

    @BeforeEach
    void setUp() {
        vm = VendingMachineFactory.createVendingMachine();
    }

    @Test
    public void testBuyItemWithExactPrice() {
        long price = vm.selectItemAndGetPrice(Item.COKE);
        assertEquals(Item.COKE.getPrice(), price);
        vm.insertCoin(Coin.QUARTER);

        Bucket<Item, List<Coin>> bucket = vm.collectItemAndChange();
        Item item = bucket.getFirst();
        List<Coin> change = bucket.getSecond();

        assertEquals(Item.COKE, item);
        assertTrue(change.isEmpty());
    }

    @Test
    public void testBuyItemWithMorePrice() {
        long price = vm.selectItemAndGetPrice(Item.SODA);
        assertEquals(Item.SODA.getPrice(), price);
        vm.insertCoin(Coin.QUARTER);
        vm.insertCoin(Coin.QUARTER);

        Bucket<Item, List<Coin>> bucket = vm.collectItemAndChange();
        Item item = bucket.getFirst();
        List<Coin> change = bucket.getSecond();

        assertEquals(Item.SODA, item);
        assertFalse(change.isEmpty());
        assertEquals(50 - Item.SODA.getPrice(), getTotal(change));
    }

    private int getTotal(List<Coin> change) {
        return change.stream()
                    .mapToInt(Coin::getDenomination)
                    .sum();
    }

    @Test
    public void testRefund() {
        long price = vm.selectItemAndGetPrice(Item.PEPSI);
        assertEquals(Item.PEPSI.getPrice(), price);
        vm.insertCoin(Coin.DIME);
        vm.insertCoin(Coin.QUARTER);
        vm.insertCoin(Coin.PENNY);
        vm.insertCoin(Coin.NICKLE);

        assertEquals(41, getTotal(vm.refund()));
    }

    @Test
    public void testSoldOut() {
        assertThrows(SoldOutException.class, () -> {
            for(int i = 0; i <= 5; i++) {
                vm.selectItemAndGetPrice(Item.COKE);
                vm.insertCoin(Coin.QUARTER);
                vm.collectItemAndChange();
            }
        });
    }

    @Test
    public void testNotSufficientChangeException() {
        assertThrows(NotSufficientChangeException.class, () -> {
            for(int i = 0; i < 5; i++) {
                vm.selectItemAndGetPrice(Item.SODA);
                vm.insertCoin(Coin.QUARTER);
                vm.insertCoin(Coin.QUARTER);
                vm.collectItemAndChange();

                vm.selectItemAndGetPrice(Item.PEPSI);
                vm.insertCoin(Coin.QUARTER);
                vm.insertCoin(Coin.QUARTER);
                vm.collectItemAndChange();
            }
        });
    }

    @Test
    public void testReset() {
        vm.reset();
        assertThrows(SoldOutException.class, () -> {
            vm.selectItemAndGetPrice(Item.COKE);
        });
    }
}