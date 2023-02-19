import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { AuthorDetailsPageRoutingModule } from './author-details-routing.module';

import { AuthorDetailsPage } from './author-details.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    AuthorDetailsPageRoutingModule
  ],
  declarations: [AuthorDetailsPage]
})
export class AuthorDetailsPageModule {}
