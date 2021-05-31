import {Component} from '@angular/core'
import {Router} from "@angular/router";
import {FormArray, FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {faArrowUp} from "@fortawesome/free-solid-svg-icons/faArrowUp";
import {AuswahlComponent} from "../auswahl/auswahl.component";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})

export class HomeComponent {
  arrowUp = faArrowUp;
  form: FormGroup;
  discounterBox = [
    {id: 100, name: 'PENNY.'},
    {id: 200, name: 'LIDL'}
  ];

  get ordersFormArray() {
    return this.form.controls.dicounters as FormArray;
  }

  constructor(private router: Router,
              private formBuilder: FormBuilder, private auswahlComponent:AuswahlComponent) {
    this.form = this.formBuilder.group({
      dicounters: new FormArray([])
    });
    this.addCheckboxes();

  }

  private addCheckboxes() {
    this.discounterBox.forEach( value =>{
      this.ordersFormArray.push(new FormControl(true));
    });
  }

  submit() {
    const selectedOrderIds = this.form.value.dicounters
      .map((checked, i) => checked ? this.discounterBox[i].name : null)
      .filter(v => v !== null);

  }
}
