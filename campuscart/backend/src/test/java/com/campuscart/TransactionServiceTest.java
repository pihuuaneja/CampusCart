package com.campuscart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.campuscart.exception.ApiException;
import com.campuscart.model.ListingView;
import com.campuscart.model.TransactionView;
import com.campuscart.repository.ListingRepository;
import com.campuscart.repository.TransactionRepository;
import com.campuscart.service.TransactionService;

class TransactionServiceTest {
    private static final long SELLER = 1L;
    private static final long BUYER = 2L;

    private TransactionRepository txs;
    private ListingRepository listings;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        txs = mock(TransactionRepository.class);
        listings = mock(ListingRepository.class);
        service = new TransactionService(txs, listings);
    }

    private ListingView listing(long sellerId, String status) {
        return new ListingView(1L, sellerId, "Seller", "Hostel A", 1, "Books", "Data Structures",
                "Good condition", BigDecimal.valueOf(300), "SELL", null, false, null, status, Instant.now());
    }

    private void givenTx(String status) {
        when(txs.findById(10L)).thenReturn(Optional.of(new TransactionView(10L, 1L, "Data Structures",
                BigDecimal.valueOf(300), BigDecimal.valueOf(250), "SELL", BUYER, "Buyer", SELLER, "Seller",
                status, null, Instant.now(), Instant.now())));
    }

    @Test
    void cannotRequestOwnListing() {
        when(listings.findById(1L)).thenReturn(Optional.of(listing(5L, "AVAILABLE")));
        ApiException ex = assertThrows(ApiException.class, () -> service.request(5L, 1L, null, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void cannotRequestUnavailableListing() {
        when(listings.findById(1L)).thenReturn(Optional.of(listing(5L, "SOLD")));
        ApiException ex = assertThrows(ApiException.class, () -> service.request(BUYER, 1L, null, null));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void requestWithoutOfferUsesAskingPrice() {
        when(listings.findById(1L)).thenReturn(Optional.of(listing(SELLER, "AVAILABLE")));
        when(txs.hasOpenRequest(1L, BUYER)).thenReturn(false);
        when(txs.create(1L, BUYER, "hi", BigDecimal.valueOf(300))).thenReturn(10L);
        givenTx("REQUESTED");
        service.request(BUYER, 1L, "hi", null);
        verify(txs).create(1L, BUYER, "hi", BigDecimal.valueOf(300));
    }

    @Test
    void sellerAcceptingReservesListing() {
        givenTx("REQUESTED");
        service.updateStatus(SELLER, 10L, "ACCEPT", null);
        verify(txs).updateStatus(10L, "ACCEPTED");
        verify(listings).updateStatus(1L, "RESERVED");
        verify(txs).rejectOtherRequests(1L, 10L);
    }

    @Test
    void buyerCannotAcceptOwnOffer() {
        givenTx("REQUESTED");
        ApiException ex = assertThrows(ApiException.class, () -> service.updateStatus(BUYER, 10L, "ACCEPT", null));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void sellerCounterPassesTurnToBuyer() {
        givenTx("REQUESTED");
        service.updateStatus(SELLER, 10L, "COUNTER", BigDecimal.valueOf(280));
        verify(txs).updateOffer(10L, "COUNTERED", BigDecimal.valueOf(280));
    }

    @Test
    void buyerCounterPassesTurnBackToSeller() {
        givenTx("COUNTERED");
        service.updateStatus(BUYER, 10L, "COUNTER", BigDecimal.valueOf(265));
        verify(txs).updateOffer(10L, "REQUESTED", BigDecimal.valueOf(265));
    }

    @Test
    void buyerAcceptsSellersCounterOffer() {
        givenTx("COUNTERED");
        service.updateStatus(BUYER, 10L, "ACCEPT", null);
        verify(txs).updateStatus(10L, "ACCEPTED");
        verify(listings).updateStatus(1L, "RESERVED");
    }

    @Test
    void sellerCannotAcceptTheirOwnCounter() {
        givenTx("COUNTERED");
        assertThrows(ApiException.class, () -> service.updateStatus(SELLER, 10L, "ACCEPT", null));
    }

    @Test
    void counterNeedsAPrice() {
        givenTx("REQUESTED");
        ApiException ex = assertThrows(ApiException.class, () -> service.updateStatus(SELLER, 10L, "COUNTER", null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void completingSaleMarksListingSold() {
        givenTx("ACCEPTED");
        service.updateStatus(SELLER, 10L, "COMPLETE", null);
        verify(listings).updateStatus(1L, "SOLD");
    }

    @Test
    void strangerCannotTouchTransaction() {
        givenTx("REQUESTED");
        assertThrows(ApiException.class, () -> service.updateStatus(99L, 10L, "CANCEL", null));
        verify(txs, never()).updateStatus(anyLong(), anyString());
    }
}
