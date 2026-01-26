import api from '@/services/axios';

export const ownerService = {
  createListing: async (listingData) => {
    // Matches @PostMapping("/add") in ListingController.java
    const { data } = await api.post('/listings/add', listingData);
    return data;
  }
};