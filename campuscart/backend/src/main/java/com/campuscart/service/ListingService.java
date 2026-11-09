package com.campuscart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.campuscart.dto.ListingFilter;
import com.campuscart.dto.ListingRequest;
import com.campuscart.exception.ApiException;
import com.campuscart.model.ListingView;
import com.campuscart.repository.CategoryRepository;
import com.campuscart.repository.ListingRepository;

@Service
public class ListingService {
    private final ListingRepository listings;
    private final CategoryRepository categories;

    public ListingService(ListingRepository listings, CategoryRepository categories) {
        this.listings = listings;
        this.categories = categories;
    }

    public List<ListingView> search(ListingFilter f) { return listings.search(f); }

    public List<ListingView> mine(long userId) { return listings.findBySeller(userId); }

    public ListingView get(long id) {
        return listings.findById(id).orElseThrow(() -> ApiException.notFound("Listing"));
    }

    public ListingView create(long userId, ListingRequest r) {
        validate(r);
        return get(listings.create(userId, r));
    }

    public ListingView update(long userId, long id, ListingRequest r) {
        validate(r);
        requireOwner(userId, get(id));
        listings.update(id, r);
        return get(id);
    }

    public void delete(long userId, long id) {
        requireOwner(userId, get(id));
        listings.delete(id);
    }

    private void validate(ListingRequest r) {
        if (!categories.exists(r.categoryId())) throw ApiException.badRequest("Unknown category");
        if (r.imageUrl() != null && !r.imageUrl().isBlank() && !r.imageUrl().startsWith("/uploads/")) {
            throw ApiException.badRequest("Upload the photo from your device");
        }
        if ("RENT".equals(r.type()) && r.rentPeriod() == null) {
            throw ApiException.badRequest("Choose a rent period (per day, week or month)");
        }
    }

    private void requireOwner(long userId, ListingView l) {
        if (l.sellerId() != userId) throw ApiException.forbidden("You can only change your own listings");
    }
}
