import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/server-information',
    pathMatch: 'full'
  },
  {
    path: 'folder/:id',
    loadChildren: () => import('./folder/folder.module').then( m => m.FolderPageModule)
  },
  {
    path: 'server-information',
    loadChildren: () => import('./server-information/server-information.module').then( m => m.ServerInformationPageModule)
  },
  {
    path: 'tei-repositories',
    loadChildren: () => import('./tei-repositories/tei-repositories.module').then( m => m.TeiRepositoriesPageModule)
  },
  {
    path: 'relocations',
    loadChildren: () => import('./relocations/relocations.module').then( m => m.RelocationsPageModule)
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
