import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {AboutComponent} from "./about/about.component";
import {PostsComponent} from "./posts/posts.component";
import {PostComponent} from "./post/post.component";
import {ManageComponent} from "./manage/manage.component";


const routes: Routes = [
  {path: '',component: HomeComponent},
  {path: 'about',component: AboutComponent},
  {path: 'posts',component: PostsComponent},
  {path: 'posts/:id', component: PostComponent},
  {path: 'manage', component: ManageComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
