import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {HomeComponent} from "./home/home.component";
import {PostComponent} from "./post/post.component";
import {PostsComponent} from "./posts/posts.component";
import {AboutComponent} from "./about/about.component";
import { ManageComponent } from './manage/manage.component';


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    PostComponent,
    PostsComponent,
    AboutComponent,
    ManageComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
