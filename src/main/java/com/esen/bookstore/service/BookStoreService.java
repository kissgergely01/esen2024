package com.esen.bookstore.service;

import com.esen.bookstore.model.Book;
import com.esen.bookstore.model.Bookstore;
import com.esen.bookstore.repository.BookRepository;
import com.esen.bookstore.repository.BookstoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookStoreService {
    private final BookstoreRepository bookstoreRepository;
    private final BookRepository bookRepository;
    @Transactional
    public void removeBookFromInventory(Book book){
        bookstoreRepository.findAll().forEach(bookstore -> {bookstore.getInventory().remove(book);
        bookstoreRepository.save(bookstore);
        });
    }

    public void save(Bookstore bookstore) {
        bookstoreRepository.save(bookstore);
    }

    public void deleteBookstore(Long id) {
        var bookstore = bookstoreRepository.findById(id).orElseThrow(() -> new RuntimeException("cannot find bookstore"));
        bookstoreRepository.delete(bookstore);
    }

    public List<Bookstore> findAll() {
        return bookstoreRepository.findAll();
    }

    public void updateBookstore(Long id, String location, Double priceModifier, Double moneyInCashRegister) {
        if(Stream.of(location,priceModifier,moneyInCashRegister).allMatch(Objects::isNull)){
            throw new UnsupportedOperationException("There is nothing to update");
        }
        var bookstore = bookstoreRepository.findById(id).orElseThrow(() -> new RuntimeException("cannot find bookstore"));

        if (location != null){
            bookstore.setLocation(location);
        }
        if (priceModifier != null){
            bookstore.setPriceModifier(priceModifier);
        }
        if (moneyInCashRegister != null){
            bookstore.setMoneyInCashRegister(moneyInCashRegister);
        }

        bookstoreRepository.save(bookstore);
    }

    public Map<Bookstore,Double> findPrices(Long id) {
        var book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("cannot find book"));
        var bookStores = bookstoreRepository.findAll();

        Map<Bookstore, Double> PriceMap = new HashMap<>();

        for(var b:bookStores){
            if(b.getInventory().containsKey(book)){
                Double currentPrice = book.getPrice() * b.getPriceModifier();
                PriceMap.put(b, currentPrice);
            }
        }
        return PriceMap;
    }
    public Map<Book, Integer> getStock(Long id){
        var bookStore = bookstoreRepository.findById(id).orElseThrow(()->new RuntimeException("cannot find bookstore"));
        return bookStore.getInventory();
    }
    public void changeStock(Long bookStoreID, Long bookID, int amount){
        var bookStore = bookstoreRepository.findById(bookStoreID).orElseThrow(()->new RuntimeException("cannot find bookstore"));
        var book = bookRepository.findById(bookID).orElseThrow(() -> new RuntimeException("cannot find book"));
        if(bookStore.getInventory().containsKey(book)){
            var entry = bookStore.getInventory().get(book);
            if (entry + amount < 0){throw new UnsupportedOperationException("Invalid amount");}
            bookStore.getInventory().replace(book,entry+amount);
        }else {
            if (amount < 0){throw new UnsupportedOperationException("Invalid amount");}
            bookStore.getInventory().put(book,amount);
        }
        bookstoreRepository.save(bookStore);
    }
}
