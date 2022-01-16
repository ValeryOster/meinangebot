import { Component, OnInit } from '@angular/core';
import {Discounter, DiscounterService} from "../../service/discounter.service";
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
    {id: 100, name: 'PENNY'},
    {id: 200, name: 'LIDL'},
    {id: 300, name: 'ALDI'},
    {id: 400, name: 'NETTO'}

  ];
  discounters: Discounter[];
  buttonActive = true;
  checkBoxStatus = false;
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
    this.buttonActive = true;
    this.checkBoxStatus = true;
    let list = [] ;
    this.form.value.dicounters.map((checked, i) =>{
        if(checked){
          var str:string;
          str = this.discounterBox[i].name
          list.push(str);
        }});
    this.discService.startGather(list).toPromise()
      .then(value => {
        console.log(this.buttonActive);
        this.buttonActive = false;
        this.checkBoxStatus = false;
      });


  }

  private addCheckboxes() {
    this.discounterBox.forEach( value =>{
      this.ordersFormArray.push(new FormControl(true));
    });
  }

  get ordersFormArray() {
    return this.form.controls.dicounters as FormArray;
  }
}
