import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {OfferDetailDto, OfferDto} from '../models/offer.model';
import {LidlOfferService} from '../service/server/lidl-offer.service';

@Component({
  selector: 'app-lidl-offer-detail',
  templateUrl: './lidl-offer-detail.component.html',
  styleUrls: ['./lidl-offer-detail.component.css']
})
export class LidlOfferDetailComponent implements OnInit {

  offer: OfferDto;
  rawJson = '';
  loading = false;
  error = '';

  constructor(private route: ActivatedRoute,
              private router: Router,
              private lidlOfferService: LidlOfferService) {
  }

  ngOnInit(): void {
    const externalId = this.route.snapshot.paramMap.get('id');
    if (!externalId) {
      this.error = 'Angebot nicht gefunden.';
      return;
    }
    this.loading = true;
    this.lidlOfferService.getOffer(externalId).subscribe(
      (detail: OfferDetailDto) => {
        this.offer = detail.offer;
        this.rawJson = detail.rawJson;
        this.loading = false;
      },
      () => {
        this.error = 'Angebot konnte nicht geladen werden.';
        this.loading = false;
      }
    );
  }

  goBack(): void {
    this.router.navigate(['/lidl']);
  }

  formatPrice(value: number): string {
    if (value == null) {
      return '-';
    }
    return value.toFixed(2).replace('.', ',') + ' €';
  }
}
