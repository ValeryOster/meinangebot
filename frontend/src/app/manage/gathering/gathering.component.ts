import { Component, OnInit } from '@angular/core';
import {Discounter, DiscounterService} from "../../service/server/discounter.service";
import {FormArray, FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-gathering',
  templateUrl: './gathering.component.html',
  styleUrls: ['./gathering.component.css']
})
export class GatheringComponent implements OnInit {

  form: FormGroup;
  discounterBox = [
    {id: 100, name: 'PENNY', isAktiv:false},
    {id: 200, name: 'LIDL', isAktiv:false},
    {id: 300, name: 'ALDI', isAktiv:false},
    {id: 400, name: 'NETTO', isAktiv:false},
    {id: 500, name: 'EDEKA', isAktiv:false},
    {id: 600, name: 'GLOBUS', isAktiv:false}
  ];
  discounters: Discounter[];
  buttonActive = true;
  constructor(public discService:DiscounterService,
              private router: Router,
              private formBuilder: FormBuilder) {
    this.form = this.formBuilder.group({
      dicounters: new FormArray([])
    });
    this.addCheckboxes();
  }

  ngOnInit(): void {
    this.discService.getAll().subscribe(value => {
      this.discounters = value;
    });
  }

  setBtnActivate($event: Event) {
    this.buttonActive = !this.buttonActive;
  }

  submit() {
    let list = new Array() ;
    this.form.value.dicounters.map((checked, i) =>{
        if(this.discounterBox[i].isAktiv){
          let str = this.discounterBox[i].name
          list.push(str);
        }});
    this.discService.startGather(list).subscribe();
  }

  private addCheckboxes() {
    this.discounterBox.forEach( value =>{
      this.ordersFormArray.push(new FormControl(true));
    });
  }

  get ordersFormArray() {
    return this.form.controls.dicounters as FormArray;
  }

  changeAktiviti(i: number) {
    this.discounterBox[i].isAktiv = !this.discounterBox[i].isAktiv
  }
}
