import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {OfferDetailDto, OfferDto, PageResponse} from '../../models/offer.model';

export interface LidlOfferSearchParams {
  search?: string;
  category?: string;
  sort?: string;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root'
})
export class LidlOfferService {

  private readonly baseUrl = environment.apiUrl + '/api/offers';

  constructor(private http: HttpClient) {
  }

  searchOffers(params: LidlOfferSearchParams = {}): Observable<PageResponse<OfferDto>> {
    let httpParams = new HttpParams()
      .set('retailer', 'lidl')
      .set('page', String(params.page != null ? params.page : 0))
      .set('size', String(params.size != null ? params.size : 24))
      .set('sort', params.sort || 'discount');

    if (params.search) {
      httpParams = httpParams.set('search', params.search);
    }
    if (params.category) {
      httpParams = httpParams.set('category', params.category);
    }

    return this.http.get<PageResponse<OfferDto>>(this.baseUrl, {params: httpParams});
  }

  getOffer(externalId: string): Observable<OfferDetailDto> {
    return this.http.get<OfferDetailDto>(`${this.baseUrl}/${externalId}`);
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/categories`, {
      params: new HttpParams().set('retailer', 'lidl')
    });
  }
}
