import { Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import { Router } from '@angular/router';
import { FormArray, FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { faArrowUp } from '@fortawesome/free-solid-svg-icons';
import { OfferListService } from '../service/offerslist/offer-list.service';

import { ReactiveFormsModule } from '@angular/forms';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { OffersComponent } from '../offers/offers.component';

@Component({
  selector: 'app-home',
  // Angular 22 nutzt standardmäßig OnPush; der bestehende Code setzt auf die bisherige Prüfung
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ReactiveFormsModule, FaIconComponent, OffersComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  arrowUp = faArrowUp;
  form: FormGroup;
  discounterBox = [
    { id: 100, name: 'PENNY' },
    { id: 300, name: 'ALDI' },
    { id: 400, name: 'NETTO' },
    { id: 500, name: 'EDEKA' },
  ];
  private ausgewahl: Array<string>;

  get ordersFormArray() {
    return this.form.controls.dicounters as FormArray;
  }

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    public offerList: OfferListService,
  ) {
    this.form = this.formBuilder.group({
      dicounters: new FormArray([]),
    });
    this.addCheckboxes();
  }

  ngOnInit(): void {
    this.offerList.getDiscounters().subscribe((value) => {
      if (value != null) {
        return (this.ausgewahl = value);
      }
    });
  }

  private addCheckboxes() {
    this.discounterBox.forEach((value) => {
      this.ordersFormArray.push(new FormControl(true));
    });
  }

  submit() {
    this.ausgewahl = this.form.value.dicounters
      .map((checked, i) => (checked ? this.discounterBox[i].name : null))
      .filter((v) => v !== null);
    this.offerList.setDiscounters(this.ausgewahl);
  }
}
