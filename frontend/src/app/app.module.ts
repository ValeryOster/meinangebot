import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {HomeComponent} from "./home/home.component";
import { ManageComponent } from './manage/manage.component';
import { OffersComponent } from './offers/offers.component';
import { GatheringComponent } from './manage/gathering/gathering.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MatTabsModule} from "@angular/material/tabs";
import {MatCardModule} from "@angular/material/card";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {LoginComponent} from "./login/login.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {authInterceptorProviders} from "./service/security/helper/auth.interceptor";
import {MatIconModule} from "@angular/material/icon";
import { AuswahlComponent } from './auswahl/auswahl.component';


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    ManageComponent,
    OffersComponent,
    GatheringComponent,
    LoginComponent,
    AuswahlComponent
  ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        AppRoutingModule,
        HttpClientModule,
        BrowserAnimationsModule,
        MatTabsModule,
        MatCardModule,
        FontAwesomeModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule
    ],
  providers: [authInterceptorProviders],
  bootstrap: [AppComponent]
})
export class AppModule { }
