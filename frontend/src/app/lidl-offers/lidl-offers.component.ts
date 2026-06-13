import {Component, OnInit} from '@angular/core';
import {OfferDto} from '../models/offer.model';
import {LidlOfferService} from '../service/server/lidl-offer.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-lidl-offers',
  templateUrl: './lidl-offers.component.html',
  styleUrls: ['./lidl-offers.component.css']
})
export class LidlOffersComponent implements OnInit {

  offers: OfferDto[] = [];
  categories: string[] = [];
  search = '';
  selectedCategory = '';
  sort = 'discount';
  loading = false;
  error = '';
  page = 0;
  totalPages = 0;
  totalElements = 0;

  constructor(private lidlOfferService: LidlOfferService, private router: Router) {
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadOffers();
  }

  loadCategories(): void {
    this.lidlOfferService.getCategories().subscribe(
      categories => this.categories = categories,
      () => this.categories = []
    );
  }

  loadOffers(): void {
    this.loading = true;
    this.error = '';
    this.lidlOfferService.searchOffers({
      search: this.search,
      category: this.selectedCategory,
      sort: this.sort,
      page: this.page,
      size: 24
    }).subscribe(
      response => {
        this.offers = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      () => {
        this.error = 'Lidl-Angebote konnten nicht geladen werden.';
        this.loading = false;
      }
    );
  }

  onSearch(): void {
    this.page = 0;
    this.loadOffers();
  }

  onCategoryChange(): void {
    this.page = 0;
    this.loadOffers();
  }

  onSortChange(): void {
    this.page = 0;
    this.loadOffers();
  }

  previousPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadOffers();
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadOffers();
    }
  }

  openDetail(offer: OfferDto): void {
    this.router.navigate(['/lidl', offer.externalId]);
  }

  formatPrice(value: number): string {
    if (value == null) {
      return '-';
    }
    return value.toFixed(2).replace('.', ',') + ' €';
  }
}
