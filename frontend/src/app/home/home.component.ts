import {Component, OnInit} from '@angular/core'
import {Router} from "@angular/router";
import {FormArray, FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {faArrowUp} from "@fortawesome/free-solid-svg-icons/faArrowUp";
import {OfferListService} from "../service/offerslist/offer-list.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})

export class HomeComponent implements OnInit {
  arrowUp = faArrowUp;
  form: FormGroup;
  discounterBox = [
    {id: 100, name: 'PENNY'},
    {id: 200, name: 'LIDL'},
    {id: 300, name: 'ALDI'},
    {id: 400, name: 'NETTO'},
    {id: 400, name: 'EDEKA'}

  ];
  private ausgewahl: Array<string>;

  get ordersFormArray() {
    return this.form.controls.dicounters as FormArray;
  }

  constructor(private router: Router,
              private formBuilder: FormBuilder, public offerList: OfferListService) {
    this.form = this.formBuilder.group({
      dicounters: new FormArray([])
    });
    this.addCheckboxes();
  }

  ngOnInit(): void {
    this.offerList.getDiscounters().subscribe(value => {
      if (value != null) {
        return this.ausgewahl = value;
      }
    })
  }

  private addCheckboxes() {
    this.discounterBox.forEach(value => {
      this.ordersFormArray.push(new FormControl(true));
    });
  }

  submit() {
    this.ausgewahl = this.form.value.dicounters.map((checked, i) => checked ? this.discounterBox[i].name : null)
      .filter(v => v !== null);
    this.offerList.setDiscounters(this.ausgewahl);
  }
}
