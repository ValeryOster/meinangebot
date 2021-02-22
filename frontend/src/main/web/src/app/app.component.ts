import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {TokenStorageService} from "./service/security/token-storage.service";
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

  constructor( public auth:TokenStorageService, private router:Router) {
  }

  ngOnInit(): void {
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
