import {Component, OnInit} from '@angular/core';
import {BwbService} from "./service/bwb.service";
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
  posts : Post[] = [
    {title: 'Ich lerne Java', text:"Und ich lerne mit Spring Boot", id:1},
    {title: 'Ich lerne Angular', text:"Und ich lerne", id:2}
  ]
  constructor(public bwbService:BwbService) {
  }

  ngOnInit(): void {
    this.bwbService.getTitle().subscribe(srv=>{
      console.log(srv);
    })
  }

  updatePosts(post:Post) {
    this.posts.unshift(post)
  }

  removePost(id:number) {
    this.posts = this.posts.filter(value => value.id !== id);
  }
}
