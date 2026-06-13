export interface OfferDto {
  externalId: string;
  retailer: string;
  title: string;
  description: string;
  currentPrice: number;
  originalPrice: number;
  currency: string;
  imageUrl: string;
  validFrom: string;
  validTo: string;
  category: string;
  sourceUrl: string;
  brand: string;
  ean: string;
  discountPercent: number;
  actionWeek: string;
  storeBranch: string;
  offerSource: string;
  leafletId: string;
  leafletPage: number;
}

export interface OfferDetailDto {
  offer: OfferDto;
  rawJson: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
