import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { TeiRepositoriesPageRoutingModule } from './tei-repositories-routing.module';

import { TeiRepositoriesPage } from './tei-repositories.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    TeiRepositoriesPageRoutingModule
  ],
  declarations: [TeiRepositoriesPage]
})
export class TeiRepositoriesPageModule {}
