import {Component, OnInit} from '@angular/core';
import {BwbService} from "./service/bwb.service";
import {AuthService} from "./service/security/auth.service";
export interface Post {
  title: string
  text: string
  id?: number
}
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  title = 'Hier entsteht Seite mit Angeboten von allen Märkte';

  constructor(public bwbService:BwbService, public auth:AuthService) {
  }

  ngOnInit(): void {
    this.bwbService.getTitle().subscribe(srv=>{
      console.log(srv);
    })
  }

}
