import {Component} from '@angular/core'
import {Router} from "@angular/router";
import {FormArray, FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {faArrowUp} from "@fortawesome/free-solid-svg-icons/faArrowUp";

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
    {id: 200, name: 'LIDL'},
    {id: 300, name: 'NETTO'},
    {id: 400, name: 'REWE'},
    {id: 500, name: 'KAUFLAND'},
    {id: 600, name: 'ALDI'}
  ];

  get ordersFormArray() {
    return this.form.controls.dicounters as FormArray;
  }

  constructor(private router: Router,
              private formBuilder: FormBuilder) {
    this.form = this.formBuilder.group({
      dicounters: new FormArray([])
    });
    this.addCheckboxes();
  }

  private addCheckboxes() {
    this.discounterBox.forEach( value =>{
      if (value.id == 100) {
        this.ordersFormArray.push(new FormControl(true));
      }else {
        this.ordersFormArray.push(new FormControl(false));
      }
    });
  }

  submit() {
    const selectedOrderIds = this.form.value.dicounters
      .map((checked, i) => checked ? this.discounterBox[i].id : null)
      .filter(v => v !== null);
    console.log(selectedOrderIds);
  }
}
