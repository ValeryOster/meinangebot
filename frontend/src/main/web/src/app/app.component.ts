import {Component, OnInit} from '@angular/core';
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

  constructor( public auth:AuthService) {
  }

  ngOnInit(): void {
  }

}
