import { Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DiscounterService } from '../../service/server/discounter.service';
import { Router } from '@angular/router';

import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-dellastitmes',
  // Angular 22 nutzt standardmäßig OnPush; der bestehende Code setzt auf die bisherige Prüfung
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ReactiveFormsModule],
  templateUrl: './dellastitmes.component.html',
  styleUrls: ['./dellastitmes.component.css'],
})
export class DellastitmesComponent implements OnInit {
  form: FormGroup;
  discounterBox = [
    { id: 100, name: 'PENNY' },
    { id: 300, name: 'ALDI' },
  ];
  buttonActive = true;

  constructor(
    public discService: DiscounterService,
    private formBuilder: FormBuilder,
  ) {
    this.form = this.formBuilder.group({
      dicounters: new FormArray([]),
    });
    this.addCheckboxes();
  }

  ngOnInit(): void {}
  get ordersFormArray() {
    return this.form.controls.dicounters as FormArray;
  }

  submit() {
    let list = new Array();
    this.form.value.dicounters.map((checked, i) => {
      if (checked) {
        var str: string;
        str = this.discounterBox[i].name;
        list.push(str);
      }
    });
    this.discService.delLastInputs(list).subscribe();
  }
  private addCheckboxes() {
    this.discounterBox.forEach((value) => {
      this.ordersFormArray.push(new FormControl(true));
    });
  }
}
