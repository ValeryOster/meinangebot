import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {AboutComponent} from "./about/about.component";
import {ManageComponent} from "./manage/manage.component";
import {AuthGuard} from "./service/security/auth.guard";
import {LoginComponent} from "./login/login.component";


const routes: Routes = [
  {path: '',component: HomeComponent},
  {path: 'about',component: AboutComponent},
  {path: 'manage', component: ManageComponent, canActivate:[AuthGuard] },
  {path: 'about',component: AboutComponent},
  {path: 'login',component: LoginComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
